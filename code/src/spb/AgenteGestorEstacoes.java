package spb;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AgenteGestorEstacoes extends Agent {

	private int bicicletasLivres;

	@Override
	protected void setup() {
		// TODO Auto-generated method stub

		bicicletasLivres = 10;

		this.addBehaviour(new analisarEstacao());

		super.setup();
	}

	private class analisarEstacao extends CyclicBehaviour {
		private int nivelPreenchimento;
		private int capacidadeTotal;
		private int descontosAceites;
		private int descontosRecusados;

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);

			if (msg != null) {

				String content = msg.getContent();
				String[] tokens = content.split(";");
				nivelPreenchimento = Integer.parseInt(tokens[0]);
				capacidadeTotal = Integer.parseInt(tokens[1]);
				descontosAceites = Integer.parseInt(tokens[2]);
				descontosRecusados = Integer.parseInt(tokens[3]);

				String resposta = analisar(nivelPreenchimento, capacidadeTotal, descontosAceites, descontosRecusados);

				ACLMessage resp = new ACLMessage(ACLMessage.INFORM);
				resp.addReceiver(msg.getSender());

				resp.setContent(resposta);
				send(resp);

			}

			else {
				block();
			}

		}

		public String analisar(int n, int c, int da, int dr) {
			StringBuilder sb = new StringBuilder();

			/* Analisar nivel de Preenchimento */
			if (bicicletasLivres != 0) {
				if (n == 1) {

					if (bicicletasLivres >= 2) {
						sb.append("+2B;");
						bicicletasLivres -= 2;
					} else {
						if (bicicletasLivres > 0) {
							sb.append("+1B;");
							bicicletasLivres -= 1;
						}
					}
				}

				if (n == 2) {
					/* Se nivel medio, entao o nmr de bicicletas deve ser incrementado em 1 */
					if (bicicletasLivres > 0) {
						sb.append("+1B;");
						bicicletasLivres -= 1;
					}
				}
			}
			if (n == 3) {
				/* Se nivel alto, entao o nmr de bicicletas deve ser decrementado em 2 */
				if (capacidadeTotal > 2) {
					sb.append("-2B;");
					bicicletasLivres += 2;
				}

				if (capacidadeTotal == 2) {
					sb.append("-3B;");
					bicicletasLivres += 1;
				}
			}

			/* Analisar se deve aumentar os descontos */

			if (descontosRecusados > (2 * descontosAceites)) {
				sb.append("+D;");
			}

			if (descontosRecusados < (2 * descontosAceites)) {
				sb.append("-D;");
			}

			/* Analisar se deve aumentar o raio */

			if (2 * (descontosAceites + descontosRecusados) < capacidadeTotal) {
				sb.append("+R;");
			}

			return sb.toString();
		}

	}

	@Override
	protected void takeDown() {
		// TODO Auto-generated method stub
		super.takeDown();
	}

}