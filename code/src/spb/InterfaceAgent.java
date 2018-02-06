package spb;

import javax.sound.midi.MetaMessage;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class InterfaceAgent extends Agent {

	private AID[] Agents;
	private AID[] Users;
	private Map<AID, Estacao> estacoes;
	private int nUtilizadores;
	private Map<LocalTime, Integer> timestamp;
	private int flag = 0;

	Frame1 f;

	protected void setup() {
		super.setup();

		this.addBehaviour(new searchEstacao());
		estacoes = new HashMap<AID, Estacao>();
		timestamp = new LinkedHashMap<LocalTime, Integer>(); // garantir ordem de entrada
		this.addBehaviour(new EstacaoRequest());

		this.addBehaviour(new TickerBehaviour(this, 200) {
			protected void onTick() {
				myAgent.addBehaviour(new showInterface());
			}
		});

	}

	private class showInterface extends OneShotBehaviour {
		int time = 0;// forma de so fazer LocalTime de 2 em 2 segundos

		@Override
		public void action() {
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					addBehaviour(new searchUtilizadores());
					if (time == 0 || time == 200) {

						if (timestamp.size() < 3) {
							LocalTime now = LocalTime.now();
							timestamp.put(now, nUtilizadores);
						} else {
							LocalTime now = LocalTime.now();
							Entry<LocalTime, Integer> entry = timestamp.entrySet().iterator().next();
							LocalTime key = entry.getKey();
							timestamp.remove(key);
							timestamp.put(now, nUtilizadores);
						}
						time = 2;
					} else
						time += 2;

					try {
						if (flag == 0) {
							f = new Frame1(estacoes, timestamp, nUtilizadores);
							flag++;
						} else {
							f.atualizaJanela(estacoes, timestamp, nUtilizadores);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			});
		}

	}

	private class searchUtilizadores extends OneShotBehaviour {

		@Override
		public void action() {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("Utilizador");
			template.addServices(sd);
			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);
				nUtilizadores = result.length;
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}

	}

	private class searchEstacao extends OneShotBehaviour {

		@Override
		public void action() {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("Estacao");
			template.addServices(sd);

			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);
				Agents = new AID[result.length];

				for (int i = 0; i < result.length; i++) {
					Agents[i] = result[i].getName();
				}

			} catch (FIPAException fe) {
				fe.printStackTrace();
			}

			if (Agents.length != 0) {
				for (int i = 0; i < Agents.length; i++) {
					estacoes.put(Agents[i], new Estacao());
				}
			}
		}

	}

	private class EstacaoRequest extends CyclicBehaviour {

		AID estacao;
		Estacao est;

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String content = msg.getContent();
				estacao = msg.getSender();
				// nome;x:y;nB;CTotal;DescA;DescR
				String[] tokens = content.split(";");

				if (!estacoes.containsKey(estacao)) {
					est = new Estacao(tokens[0], tokens[1], Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]),
							Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]));
					estacoes.put(estacao, est);
				} else {
					estacoes.get(estacao).setNome(tokens[0]);
					estacoes.get(estacao).setCoordenadas(tokens[1]);
					estacoes.get(estacao).setnBicicletas(Integer.parseInt(tokens[2]));
					estacoes.get(estacao).setCapacidade(Integer.parseInt(tokens[3]));
					estacoes.get(estacao).setDescontosAceites(Integer.parseInt(tokens[4]));
					estacoes.get(estacao).setDescontosRecusados(Integer.parseInt(tokens[5]));
				}

			} else {
				block();
			}

		}

	}

	@Override
	protected void takeDown() {
		super.takeDown();
	}

}

class Estacao {
	String nome;
	String coordenadas;
	int nBicicletas;
	int capacidade;
	int descAceites;
	int descRecusados;

	public Estacao() {
		this.nome = "a";
		this.coordenadas = "";
		this.nBicicletas = 0;
		this.capacidade = 0;
		this.descAceites = 0;
		this.descRecusados = 0;
	}

	public Estacao(String nome, String c, int nB, int cap, int d1, int d2) {
		this.nome = nome;
		this.coordenadas = c;
		this.nBicicletas = nB;
		this.capacidade = cap;
		this.descAceites = d1;
		this.descRecusados = d2;
	}

	public Estacao(Estacao e) {
		this.nome = e.getNome();
		this.coordenadas = e.getCoordenadas();
		this.nBicicletas = e.getnBicicletas();
		this.capacidade = e.getCapacidade();
		this.descAceites = e.getDescontosAceites();
		this.descRecusados = e.getDescontosRecusados();
	}

	public String getNome() {
		return this.nome;
	}

	public String getCoordenadas() {
		return this.coordenadas;
	}

	public int getnBicicletas() {
		return this.nBicicletas;
	}

	public int getCapacidade() {
		return this.capacidade;
	}

	public int getDescontosAceites() {
		return this.descAceites;
	}

	public int getDescontosRecusados() {
		return this.descRecusados;
	}

	public Estacao clone() {
		return new Estacao(this);
	}

	public void setNome(String n) {
		this.nome = n;
	}

	public void setCoordenadas(String coord) {
		this.coordenadas = coord;
	}

	public void setnBicicletas(int n) {
		this.nBicicletas = n;
	}

	public void setCapacidade(int c) {
		this.capacidade = c;
	}

	public void setDescontosAceites(int c) {
		this.descAceites = c;
	}

	public void setDescontosRecusados(int c) {
		this.descRecusados = c;
	}

}
