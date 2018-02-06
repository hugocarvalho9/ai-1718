package spb;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import static java.lang.Math.sqrt;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class AgenteUtilizador extends Agent {

	/* Posicao */
	private int posEixoX;
	private int posEixoY;

	/* Estacao inicial/Estacao final */
	private String estacaoInicial;
	private String estacaoDestino;

	/* Coordenadas da estacao final */
	private int estacaoDestinoX;
	private int estacaoDestinoY;

	/* Fatores do Utilizador */
	private int condFisica;
	private int idade;

	/* Fator do Agente Informacao Geral */
	private float factor_externo;

	private float factor_total;

	private String decisao;

	private int flag = 0;

	/* Array com todas as estacoes */
	private AID[] Estacoes;

	@Override
	public void setup() {

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Utilizador");
		sd.setName("JADE-Utilizador");
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		estacaoDestino = null;

		Random rand = new Random();
		condFisica = rand.nextInt(3) + 1;
		idade = rand.nextInt(3) + 1;

		this.addBehaviour(new searchEstacoes());

		startEstacao();
		endEstacao();

		/* recebe informacao geral */
		this.addBehaviour(new receiveMsgs());

		/* muda de posicao e informa as estacoes de 1 em 1s */
		this.addBehaviour(new novaPos(this, 200));
		this.addBehaviour(new informPos(this, 200));

	}

	private class searchEstacoes extends OneShotBehaviour {
		@Override
		public void action() {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("Estacao");
			template.addServices(sd);

			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);
				Estacoes = new AID[result.length];
				for (int i = 0; i < result.length; i++) {
					Estacoes[i] = result[i].getName();
				}
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}

	}

	private void startEstacao() {
		int randomNum = ThreadLocalRandom.current().nextInt(0, 4);
		switch (randomNum) {
		case 0:
			posEixoX = 6000;
			posEixoY = 2000;
			estacaoInicial = "Estacao1";
			break;

		case 1:
			posEixoX = 1000;
			posEixoY = 5000;
			estacaoInicial = "Estacao2";
			break;

		case 2:
			posEixoX = 4000;
			posEixoY = 4000;
			estacaoInicial = "Estacao3";
			break;

		case 3:
			posEixoX = 8000;
			posEixoY = 4000;
			estacaoInicial = "Estacao4";
			break;

		case 4:
			posEixoX = 2000;
			posEixoY = 2000;
			estacaoInicial = "Estacao5";
			break;

		}

		AID dest = new AID(estacaoInicial, AID.ISLOCALNAME);
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(dest);
		msg.setContent("-1B");
		send(msg);

	}

	private void endEstacao() {

		while (estacaoDestino == null) {
			int randomNum = ThreadLocalRandom.current().nextInt(0, 4);
			int aux = randomNum + 1;
			if (!estacaoInicial.equals("Estacao" + aux)) {
				switch (randomNum) {
				case 0:
					estacaoDestinoX = 6000;
					estacaoDestinoY = 2000;
					estacaoDestino = "Estacao1";
					break;

				case 1:
					estacaoDestinoX = 1000;
					estacaoDestinoY = 5000;
					estacaoDestino = "Estacao2";
					break;

				case 2:
					estacaoDestinoX = 4000;
					estacaoDestinoY = 4000;
					estacaoDestino = "Estacao3";
					break;

				case 3:
					estacaoDestinoX = 8000;
					estacaoDestinoY = 4000;
					estacaoDestino = "Estacao4";
					break;

				case 4:
					estacaoDestinoX = 2000;
					estacaoDestinoY = 2000;
					estacaoDestino = "Estacao5";
					break;

				}
			}

		}

	}

	private class receiveMsgs extends CyclicBehaviour {

		@Override
		public void action() {
			// TODO Auto-generated method stub
			ACLMessage msg = myAgent.receive();

			if (msg != null) {
				if (msg.getPerformative() == 7) {
					String msg_res = msg.getContent();

					if (msg_res.equals("X"))
						myAgent.doDelete();

					String[] tokens = msg_res.split(":");
					if (tokens[0].equals("FA"))
						factor_externo = Float.parseFloat(tokens[1]);
				}

				if (msg.getPerformative() == 3) {
					if (flag != 1) {
						String msg_res = msg.getContent();
						String[] tokens = msg_res.split(";");

						factor_total = factor_total(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
						decisao = decisao(factor_total, Integer.parseInt(tokens[0]));

						if (decisao.equals("SIM")) {
							actualiza_destino(msg.getSender().getLocalName(), Integer.parseInt(tokens[1]),
									Integer.parseInt(tokens[2]));
							ACLMessage msg1 = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
							//System.out.println("ACEITEI A PROPOSTA -> " + factor_total);
							msg1.addReceiver(msg.getSender());
							msg1.setContent(decisao);
							send(msg1);
							flag = 1;
						}

						if (decisao.equals("NAO")) {
							ACLMessage msg1 = new ACLMessage(ACLMessage.REFUSE);
							//System.out.println("RECUSEI A PROPOSTA ->" + factor_total);
							msg1.addReceiver(msg.getSender());
							msg1.setContent(decisao);
							send(msg1);
						}
					}

				}
			} else {
				block();
			}
		}

	}

	private class novaPos extends TickerBehaviour {

		public novaPos(Agent t, long time) {
			super(t, time);
		}

		@Override
		protected void onTick() {
			// TODO Auto-generated method stub
			if (estacaoDestinoX > posEixoX && estacaoDestinoY > posEixoY) {
				posEixoX += 10;
				posEixoY += 10;
			}
			if (estacaoDestinoX < posEixoX && estacaoDestinoY > posEixoY) {
				posEixoX -= 10;
				posEixoY += 10;
			}
			if (estacaoDestinoX > posEixoX && estacaoDestinoY < posEixoY) {
				posEixoX += 10;
				posEixoY -= 10;
			}
			if (estacaoDestinoX < posEixoX && estacaoDestinoY < posEixoY) {
				posEixoX -= 10;
				posEixoY -= 10;
			}
			if (estacaoDestinoY > posEixoY)
				posEixoY += 10;
			if (estacaoDestinoY < posEixoY)
				posEixoY -= 10;
			if (estacaoDestinoX > posEixoX)
				posEixoX += 10;
			if (estacaoDestinoX < posEixoX)
				posEixoX -= 10;

			if ((estacaoDestinoX == posEixoX) && (estacaoDestinoY == posEixoY)) {
				entregaBicicleta();
				try {
					DFService.deregister(myAgent);
				} catch (FIPAException e) {
					e.printStackTrace();
				}
				myAgent.doDelete();
				
			}
			/*System.out.println("Sou o utilizador " + myAgent.getName() + " e estou na posicao " + posEixoX + " : "
					+ posEixoY + " e vou para " + estacaoDestinoX + " : " + estacaoDestinoY); */
		}

	}

	public void entregaBicicleta() {
		AID dest = new AID(estacaoDestino, AID.ISLOCALNAME);
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(dest);
		msg.setContent("E");
		send(msg);
	}

	private class informPos extends TickerBehaviour {

		public informPos(Agent t, long time) {
			super(t, time);
		}

		protected void onTick() {
			enviaMsg();
		}

	}

	public void enviaMsg() {

		ParallelBehaviour b = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ALL) {

			public int onEnd() {
				return 0;
			}
		};

		this.addBehaviour(b);
		b.addSubBehaviour(new sendMessageToEstacoes());

	}

	private class sendMessageToEstacoes extends SimpleBehaviour {
		int i = 0;

		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(Estacoes[i]);
			// U;x;y
			msg.setContent("U;" + posEixoX + ";" + posEixoY);
			i++;
			send(msg);
		}

		@Override
		public boolean done() {
			if (i < Estacoes.length) {
				return false;
			}
			return true;
		}
	}

	public float factor_total(int destino_novo_X, int destino_novo_Y) {
		double cateto1 = destino_novo_X - posEixoX;
		double cateto2 = destino_novo_Y - posEixoY;
		double dist = sqrt(cateto1 * cateto1 + cateto2 * cateto2);

		int dist_nivel = 1;
		if (dist <= 1000)
			dist_nivel = 3;
		if (dist > 1000 && dist < 2000)
			dist_nivel = 2;
		if (dist >= 2000)
			dist_nivel = 1;

		float factor_interno = (float) (dist_nivel + idade + condFisica) / 3;
		return (4 * factor_externo + 3 * factor_interno) / 7;

	}

	public String decisao(float factor_total, int desconto) {

		if ((factor_total < 1.7 && desconto >= 75)
				|| (factor_total >= 1.7 && factor_total <= 2.4 && desconto < 75 && desconto >= 50)
				|| (factor_total > 2.4 && desconto < 50))
			return "SIM";

		return "NAO";
	}

	public void actualiza_destino(String nome, int x, int y) {
		estacaoDestino = nome;
		estacaoDestinoX = x;
		estacaoDestinoY = y;
	}
}
