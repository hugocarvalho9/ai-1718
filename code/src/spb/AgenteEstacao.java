package spb;

import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import static java.lang.Math.sqrt;

import java.awt.EventQueue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AgenteEstacao extends Agent {

	private int capacidadeMaxima;
	private int stockAtual;

	/* 1 -> baixo; 2 -> medio; 3 -> alto */
	private int nivelPreenchimento;

	/* raio da area de proximidade */
	private double areaProximidade;

	/* coordenadas da estacao */
	private int x;
	private int y;

	/* descontos aceites/recusados */
	private int descontosAceites;
	private int descontosRecusados;
	private List<AID> clientes_recusados;

	private int[] descontos = new int[3];
	private String LocalName = this.getLocalName();

	@Override
	protected void setup() {
		clientes_recusados = new ArrayList<>();
		capacidadeMaxima = 12;
		stockAtual = 10;
		atualizaNivel(stockAtual, capacidadeMaxima);
		areaProximidade = 5000;

		descontos[0] = 80;
		descontos[1] = 70;
		descontos[2] = 15;

		setPosition();

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Estacao");
		sd.setName("JADE-Estacao");
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		// this.addBehaviour(new imprimeDados(this, 1000));
		this.addBehaviour(new enviarStatusGestor(this, 1000));
		this.addBehaviour(new atualizaDadosGestor());
		this.addBehaviour(new TickerBehaviour(this, 200) {
			protected void onTick() {
				myAgent.addBehaviour(new enviarStatusInterface());
			}
		});
		super.setup();
	}

	private void setPosition() {
		switch (this.getLocalName()) {
		case "Estacao1":
			x = 6000;
			y = 2000;
			break;

		case "Estacao2":
			x = 1000;
			y = 5000;
			break;

		case "Estacao3":
			x = 4000;
			y = 4000;
			break;

		case "Estacao4":
			x = 8000;
			y = 4000;
			break;

		case "Estacao5":
			x = 2000;
			y = 2000;
			break;
		}

	}

	private class imprimeDados extends TickerBehaviour {

		public imprimeDados(Agent t, long time) {
			super(t, time);
		}

		@Override
		protected void onTick() {
			// TODO Auto-generated method stub
			System.out.println("--------------------------------");
			System.out.println("Sou a Estacao " + getAID().getName());
			System.out.println("Capacidade Maxima -> " + capacidadeMaxima);
			System.out.println("Stock Atual -> " + stockAtual);
			System.out.println("Nivel -> " + nivelPreenchimento);
			System.out.println("Area -> " + areaProximidade);
			System.out.println("DESCONTOS ACEITES -> " + descontosAceites);
			System.out.println("DESCONTOS NEGADOS -> " + descontosRecusados);
			System.out.println("--------------------------------");

			/*
			 * AID dest = new AID("InterfaceAgent",AID.ISLOCALNAME); ACLMessage msg = new
			 * ACLMessage(ACLMessage.INFORM); msg.addReceiver(dest);
			 * msg.setContent(x+";"+y+";"+stockAtual+";"+capacidadeMaxima); send(msg);
			 */

		}

	}

	public void atualizaNivel(int s, int c) {
		double pct = ((double) s / c) * 100;

		if (pct <= 30)
			nivelPreenchimento = 1;
		else if (pct >= 70)
			nivelPreenchimento = 3;
		else
			nivelPreenchimento = 2;

	}

	private class enviarStatusGestor extends TickerBehaviour {

		public enviarStatusGestor(Agent t, long time) {
			super(t, time);
		}

		@Override
		protected void onTick() {
			// TODO Auto-generated method stub
			AID dest = new AID("AgenteGestorEstacoes", AID.ISLOCALNAME);
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(dest);
			msg.setContent(
					nivelPreenchimento + ";" + capacidadeMaxima + ";" + descontosAceites + ";" + descontosRecusados);
			send(msg);
		}
	}

	private class enviarStatusInterface extends OneShotBehaviour {

		@Override
		public void action() {
			// TODO Auto-generated method stub
			AID dest = new AID("InterfaceAgent", AID.ISLOCALNAME);
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(dest);
			msg.setContent(LocalName + ";" + x + ":" + y + ";" + stockAtual + ";" + capacidadeMaxima + ";"
					+ descontosAceites + ";" + descontosRecusados);
			send(msg);
		}

	}

	private class atualizaDadosGestor extends CyclicBehaviour {

		@Override
		public void action() {

			ACLMessage msg = receive();
			String content;

			if (msg != null) {
				if (msg.getPerformative() == 7) {
					/* ver se a msg vem do utilizador ou do gestor */

					content = msg.getContent();
					String[] tokens = content.split(";");

					if (tokens[0].equals("U")) {
						// recebe msg do utilizador com atualizacao da posicao

						int posX = Integer.parseInt(tokens[1]);
						int posY = Integer.parseInt(tokens[2]);
						double h = (posX - x) * (posX - x) + (posY - y) * (posY - y);
						double dist = sqrt(h);

						if (dist < areaProximidade) {
							ACLMessage proposta = new ACLMessage(ACLMessage.CFP);
							proposta.addReceiver(msg.getSender());
							int desconto = calcula(dist); // x;y
							proposta.setContent(desconto + ";" + x + ";" + y);
							send(proposta);
						}

					}

					else {

						for (int i = 0; i < tokens.length; i++) {
							switch (tokens[i]) {

							case "+2B":
								// capacidadeMaxima += 2;
								stockAtual += 2;
								break;

							case "+1B":
								// capacidadeMaxima++;
								stockAtual++;
								break;

							case "-2B":
								if (stockAtual > 2)
									stockAtual -= 2;
								break;

							case "-3B":
								if (stockAtual > 1)
									stockAtual -= 1;
								break;

							case "+R":
								areaProximidade += 500;
								break;

							case "+D":
								if (descontos[0] < 90)
									descontos[0] += 5;
								if (descontos[1] < 80)
									descontos[1] += 5;
								if (descontos[2] < 30)
									descontos[2] += 5;
								break;

							case "-D":
								if (descontos[0] > 65)
									descontos[0] -= 5;
								if (descontos[1] > 55)
									descontos[1] -= 5;
								if (descontos[2] > 0)
									descontos[2] -= 5;
								break;

							case "-1B":
								if (stockAtual == 0) {
									ACLMessage msgR = new ACLMessage(ACLMessage.INFORM);
									msgR.addReceiver(msg.getSender());
									msgR.setContent("X");
									send(msgR);
								} else
									stockAtual--;
								break;

							// utilizador entrega bicicleta
							case "E":
								stockAtual++;

							default:
								break;
							}
						}

						atualizaNivel(stockAtual, capacidadeMaxima);
					}

				} else {
					/* Aceitou ou recusou proposta */

					if (msg.getPerformative() == 0) {
						// aceitou proposta

						descontosAceites++;
					}

					if (msg.getPerformative() == 14) {
						if (!clientes_recusados.contains(msg.getSender())) {
							descontosRecusados++;
							clientes_recusados.add(msg.getSender());
						}
					}
				}
			}
		}

	}

	public int calcula(double dist) {
		if (dist > 1000 && nivelPreenchimento == 1)
			return descontos[0];
		if (dist < 1000 && nivelPreenchimento == 1)
			return descontos[1];
		if (dist > 1000 && nivelPreenchimento == 2)
			return descontos[2];
		return 0;
	}

	@Override
	protected void takeDown() {

		super.takeDown();
	}

}
