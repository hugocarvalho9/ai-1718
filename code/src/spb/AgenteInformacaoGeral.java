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

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class AgenteInformacaoGeral extends Agent {

	private int estacaoDoAno;
	private int hora;
	private int luminosidade;
	private int humidade;
	private float factor_externo;
	private AID[] AgentesUtilizadores;

	@Override
	protected void setup() {
		super.setup();

		Random rand = new Random();
		estacaoDoAno = rand.nextInt(3) + 1;
		hora = rand.nextInt(3) + 1;
		luminosidade = rand.nextInt(3) + 1;
		humidade = rand.nextInt(3) + 1;

		this.addBehaviour(new informaUtilizadores(this, 1000));

		/* Atualiza estação do Ano de 10 em 10 min */
		this.addBehaviour(new atualizaEstacaoDoAno(this, 1800000));

		/* Atualiza hora, luminosidade e humidade de 2 em 2 segundo */
		this.addBehaviour(new atualizaHora(this, 1000));
		this.addBehaviour(new atualizaLuminosidade(this, 1000));
		this.addBehaviour(new atualizaHumidade(this, 1000));

	}

	private class informaUtilizadores extends TickerBehaviour {

		public informaUtilizadores(Agent t, long time) {
			super(t, time);
		}

		@Override
		protected void onTick() {
			// TODO Auto-generated method stub
			addBehaviour(new searchUtilizadores());
			enviaMsg();
		}

	}

	private class searchUtilizadores extends OneShotBehaviour {

		@Override
		public void action() {
			// TODO Auto-generated method stub

			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("Utilizador");
			template.addServices(sd);

			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);
				AgentesUtilizadores = new AID[result.length];

				for (int i = 0; i < result.length; i++) {
					AgentesUtilizadores[i] = result[i].getName();
				}
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}

	}

	public void enviaMsg() {

		ParallelBehaviour b = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ALL) {

			public int onEnd() {
				return 0;
			}
		};

		this.addBehaviour(b);
		b.addSubBehaviour(new sendMessageToUtilizadores());

	}

	private class sendMessageToUtilizadores extends SimpleBehaviour {

		int i = 0;

		@Override
		public void action() {
			// TODO Auto-generated method stub

			if (AgentesUtilizadores.length > 0) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(AgentesUtilizadores[i]);
				factor_externo = (float) (estacaoDoAno + humidade + luminosidade + hora) / 4;
				msg.setContent("FA:" + factor_externo);
				i++;
				send(msg);
			}
		}

		@Override
		public boolean done() {
			if (i < AgentesUtilizadores.length) {
				return false;
			}
			return true;
		}
	}

	private class atualizaEstacaoDoAno extends TickerBehaviour {
		int aux = estacaoDoAno;

		public atualizaEstacaoDoAno(Agent t, long time) {
			super(t, time);
		}

		@Override
		protected void onTick() {
			if (aux == 1) {
				estacaoDoAno = 2;
				aux = 2;
			} else if (aux == 2) {
				estacaoDoAno = 3;
				aux = 3;
			} else if (aux == 3) {
				estacaoDoAno = 2;
				aux = 4;
			} else {
				estacaoDoAno = 1;
				aux = 1;
			}
		}

	}

	private class atualizaLuminosidade extends TickerBehaviour {
		int aux = 0;

		public atualizaLuminosidade(Agent t, long time) {
			super(t, time);
		}

		@Override
		protected void onTick() {
			if (aux >= 0 && aux <= 159)
				aux++;
			else
				aux = 0;
			if (aux <= 40)
				luminosidade = 1;
			else if (aux > 40 && aux < 120)
				luminosidade = 2;
			else if (aux >= 120)
				luminosidade = 3;
		}

	}

	private class atualizaHora extends TickerBehaviour {
		int aux = 0;

		public atualizaHora(Agent t, long time) {
			super(t, time);
		}

		@Override
		protected void onTick() {
			if (aux >= 0 && aux <= 24)
				aux++;
			else
				aux = 0;
			if (aux <= 6 || aux >= 22)
				hora = 1;
			else if (aux > 6 && aux <= 14)
				hora = 2;
			else if (aux > 14 && aux < 22)
				hora = 3;
		}

	}

	private class atualizaHumidade extends TickerBehaviour {
		int aux = 50;
		int randomNum;

		public atualizaHumidade(Agent t, long time) {
			super(t, time);
		}

		@Override
		protected void onTick() {
			if (aux > 0 && aux < 100)
				randomNum = ThreadLocalRandom.current().nextInt();
			else if (aux == 0)
				randomNum = 1;
			else
				randomNum = -1;
			aux = aux + randomNum;

			if (aux <= 33)
				humidade = 1;
			else if (aux > 33 && aux < 66)
				humidade = 2;
			else if (aux > 65)
				humidade = 3;
		}

	}

	@Override
	protected void takeDown() {
		super.takeDown();
	}

}
