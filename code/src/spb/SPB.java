package spb;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SPB {

	Runtime rt;
	ContainerController container;

	public ContainerController initContainerInPlatform(String host, String port, String containerName) {
		// Get the JADE runtime interface (singleton)
		this.rt = Runtime.instance();

		// Create a Profile, where the launch arguments are stored
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, containerName);
		profile.setParameter(Profile.MAIN_HOST, host);
		profile.setParameter(Profile.MAIN_PORT, port);
		// create a non-main agent container
		ContainerController container = rt.createAgentContainer(profile);
		return container;
	}

	public void initMainContainerInPlatform(String host, String port, String containerName) {

		// Get the JADE runtime interface (singleton)
		this.rt = Runtime.instance();

		// Create a Profile, where the launch arguments are stored
		Profile prof = new ProfileImpl();
		prof.setParameter(Profile.CONTAINER_NAME, containerName);
		prof.setParameter(Profile.MAIN_HOST, host);
		prof.setParameter(Profile.MAIN_PORT, port);
		prof.setParameter(Profile.MAIN, "true");
		prof.setParameter(Profile.GUI, "true");

		// create a main agent container
		this.container = rt.createMainContainer(prof);
		rt.setCloseVM(true);

	}

	public void startAgentInPlatform(String name, String classpath) {
		try {
			AgentController ac = container.createNewAgent(name, classpath, new Object[0]);
			ac.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SPB a = new SPB();

		a.initMainContainerInPlatform("localhost", "9888", "SPB");

		a.startAgentInPlatform("AgenteGestorEstacoes", "spb.AgenteGestorEstacoes");
		a.startAgentInPlatform("AgenteInformacaoGeral", "spb.AgenteInformacaoGeral");
		a.startAgentInPlatform("InterfaceAgent", "spb.InterfaceAgent");
		a.startAgentInPlatform("Estacao1", "spb.AgenteEstacao");
		a.startAgentInPlatform("Estacao2", "spb.AgenteEstacao");
		a.startAgentInPlatform("Estacao3", "spb.AgenteEstacao");
		a.startAgentInPlatform("Estacao4", "spb.AgenteEstacao");
		a.startAgentInPlatform("Estacao5", "spb.AgenteEstacao");

		int i = 1;
		while (i <= 40) {
			try {
				a.startAgentInPlatform("Utilizador" + i, "spb.AgenteUtilizador");
				i++;
				TimeUnit.MILLISECONDS.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}