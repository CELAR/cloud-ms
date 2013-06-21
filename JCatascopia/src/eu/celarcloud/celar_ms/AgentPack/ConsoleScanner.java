package eu.celarcloud.celar_ms.AgentPack;

import java.util.InputMismatchException;
import java.util.Map.Entry;
import java.util.Scanner;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ProbePack.IProbe;
import eu.celarcloud.celar_ms.ProbePack.ProbeProperty;


public class ConsoleScanner extends Thread{

	public enum ScannerStatus{INACTIVE,ACTIVE,DYING};
	private ScannerStatus scannerStatus;
	private Scanner scanner;
	private MonitoringAgent agent;
	private boolean backFlag;
	
	public ConsoleScanner(MonitoringAgent agent){
		super("ConsoleScanner-Thread");
		this.scanner = new Scanner(System.in);
		this.scannerStatus = ScannerStatus.ACTIVE;
		this.agent = agent;
		
		this.backFlag = false;
	}
	
	public synchronized void terminate(){
		this.scannerStatus = ScannerStatus.DYING;
	}

	@Override
	public void run(){
		while(this.scannerStatus != ScannerStatus.DYING){
			
			if(this.backFlag || (scanner.next()).equals("m")){
				
				this.agent.setCollectorWritingStatus(false);
				this.backFlag = false;
				
				this.mainMenu();
			}
			this.agent.setCollectorWritingStatus(true);
		}
	}
	
	private void mainMenu(){
		System.out.println("\nMain Menu\n----------");
		System.out.println("[p]robe menu\n" +
						   "[h]elp\n" +
					       "[e]xit main menu");
		
		String input = scanner.next();
		
		if(input.equals("p"))
			this.probeMenu();
		else if(input.equals("h"))
			this.helpMenu();
		else if(!input.equals("e")){
			System.out.println("Not a valid command: "+input);
		}
	}
	
	private void probeMenu(){
		System.out.println("\nProbe Menu\n----------");
		System.out.println("[a]ctivate probe\n" +
						   "[o]activate all probes\n" +
						   "[d]eactivate probe\n" +
						   "[c]onfigure probe\n" +
						   "[l]ist available probes\n" +
						   "[i]nformation about a probe\n" +
						   "[A]dd probe\n" +
						   "[R]emove/Terminate probe\n" +
						   "[e]xit probe menu");
		
		String input = scanner.next();
		if (input.equals("l")){
			System.out.println("ProbeID\t\t\t\t\tProbeName\tProbeStatus\n------------------------------------\t------------\t-----------");
			for (Entry<String, IProbe> entry : this.agent.getProbeMap().entrySet()){
				IProbe val = entry.getValue();
				System.out.println(entry.getKey()+"\t"+val.getProbeName()+"\t"+val.getProbeStatus());
			}
		}
		else if (input.equals("a")){
			System.out.print("probe name: ");
			input = scanner.next();
			try{
				this.agent.activateProbe(input);
			}catch(CatascopiaException e){
				System.out.println(e.getMessage());
			}
		}
		else if (input.equals("o")){
			this.agent.activateAllProbes();
		}
		else if (input.equals("c")){
			System.out.print("probe name: ");
			input = scanner.next();
			try{
				IProbe probe = this.agent.getProbe(input);
				this.configProbeMenu(probe);
			}catch (CatascopiaException e){
				System.out.println(e.getMessage());
			}
		}
		else if (input.equals("i")){
			System.out.print("probe name: ");
			input = scanner.next();
			try{
				IProbe probe = this.agent.getProbe(input);
				System.out.println();
				for (Entry<String, String> entry : probe.getProbeMetadata().entrySet())
					System.out.println(entry.getKey()+ " : " + entry.getValue());
				
				System.out.println("\nProbe Properties:\n");
				for (Entry<Integer, ProbeProperty> entry : probe.getProbeProperties().entrySet()){
					ProbeProperty prop = entry.getValue();
					for (Entry<String, String> p : prop.getProbePropertyMetadata().entrySet())
						System.out.println(p.getKey()+ " : " + p.getValue());
					System.out.println();
				}
			}catch (CatascopiaException e){
				System.out.println(e.getMessage());
			}
		}
		else if (input.equals("d")){
			System.out.print("probe name: ");
			input = scanner.next();
			try{
				this.agent.deactivateProbe(input);
			}catch(CatascopiaException e){
				System.out.println(e.getMessage());
			}
		}
		else if (input.equals("A")){
			System.out.println("Command not implemented yet :(");
		}
		else if (input.equals("R")){
			System.out.print("probe name: ");
			input = scanner.next();
			try{
				this.agent.removeProbeByName(input);
			}catch (CatascopiaException e){
				System.out.println(e.getMessage());
			}
		}
		else if(input.equals("e")){
			this.backFlag = true;
			return;
		}
		else System.out.println("Not a valid command: "+input);
		
		this.probeMenu();	
	}
	
	private void helpMenu(){
		System.out.println("\nHelp Menu\n----------");
		System.out.println("Menu not implemented yet :(");
		this.backFlag = true;
	}
	
	private void configProbeMenu(IProbe probe){
		System.out.println("\nConfigure Probe Settings Menu\n-----------------------------");
		System.out.println("[f]change probe collecting frequency\n" +
				   		   "[n]change probe name\n" +
				           "[e]xit probe config menu");
		
		String input = scanner.next();
		if (input.equals("f")){
			try{
				System.out.print("set frequency (in seconds): ");
				int freq = scanner.nextInt();
				probe.setCollectFreq(freq);
			}catch(InputMismatchException e){
				System.out.println("input given is not valid, please insert an integer");
			}
		}
		else if (input.equals("n")){
			System.out.print("set name: ");
			input = scanner.next();
			this.agent.changeProbeName(probe, input);
		}
		else if(input.equals("e")){
			return;
		}
		else System.out.println("Not a valid command: "+input);
		
		this.configProbeMenu(probe);
	}
	
	/**
	 * @param args
	 * @throws CatascopiaException 
	 */
	public static void main(String[] args) throws CatascopiaException{
		MonitoringAgent magent = new MonitoringAgent();
		ConsoleScanner cs = new ConsoleScanner(magent);
		cs.start();
	}

}
