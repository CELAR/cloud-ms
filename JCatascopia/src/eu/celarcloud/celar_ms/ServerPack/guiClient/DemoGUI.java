package eu.celarcloud.celar_ms.ServerPack.guiClient;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DemoGUI extends JFrame{
	
	private static final long serialVersionUID = 1L;
	private JPanel mainPanel;
	private JPanel panel;
	
	private JLabel totalMemLabel;
	private JLabel usedMemLabel;
	private JLabel freeMemLabel;
	private JLabel cacheMemLabel;
	private JLabel totalSwapMemLabel;
	private JLabel freeSwapMemLabel;
	
	private JLabel cpuTotalUsageLabel;
	private JLabel cpuUserUsageLabel;
	private JLabel cpuSystemUsageLabel;
	private JLabel cpuIdleUsageLabel;
	private JLabel cpuIOwaitUsageLabel;
	
	private JLabel bytesINLabel;
	private JLabel bytesOUTLabel;
	private JLabel packetsINLabel;
	private JLabel packetsOUTLabel;
	
	private JLabel diskTotal;
	private JLabel diskFree;
	private JLabel diskUsed;
	
	private JLabel readkbps;
	private JLabel writekbps;
	private JLabel iotime;
	
	
	public DemoGUI(){
		super("JCatascopia Demo GUI");
	}
	
	public void init(){
		//this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//main panel holds all the other panels
		mainPanel=new JPanel();
		mainPanel.setLayout(new GridLayout(0,2,5,5));		
		this.add(mainPanel);
		
		//Memory
		panel=new JPanel();
		panel.setLayout(new GridLayout(0,2,5,5));
		panel.setBorder(BorderFactory.createTitledBorder("Memory Stats"));
		JLabel label1 = new JLabel("Total Memory: ");
		totalMemLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.totalMemLabel);
		label1 = new JLabel("Used Memory: ");
		usedMemLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.usedMemLabel);
		label1 = new JLabel("Free Memory: ");
		freeMemLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.freeMemLabel);
		label1 = new JLabel("Cached Memory: ");
		cacheMemLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.cacheMemLabel);
		label1 = new JLabel("Total Swap Memory: ");
		this.totalSwapMemLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.totalSwapMemLabel);
		label1 = new JLabel("Free Swap Memory: ");
		this.freeSwapMemLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.freeSwapMemLabel);
		mainPanel.add(panel);
		
		//CPU
		panel=new JPanel();
		panel.setLayout(new GridLayout(0,2,5,5));
		panel.setBorder(BorderFactory.createTitledBorder("CPU Stats"));
		label1 = new JLabel("Total CPU Usage: ");
		this.cpuTotalUsageLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.cpuTotalUsageLabel);
		label1 = new JLabel("User CPU Usage: ");
		this.cpuUserUsageLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.cpuUserUsageLabel);
		label1 = new JLabel("System CPU Usage: ");
		this.cpuSystemUsageLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.cpuSystemUsageLabel);
		label1 = new JLabel("CPU Idle: ");
		this.cpuIdleUsageLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.cpuIdleUsageLabel);
		label1 = new JLabel("CPU IO wait: ");
		this.cpuIOwaitUsageLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.cpuIOwaitUsageLabel);
		mainPanel.add(panel);
		
		//Network
		panel=new JPanel();
		panel.setLayout(new GridLayout(0,2,5,5));
		panel.setBorder(BorderFactory.createTitledBorder("Network Stats"));
		label1 = new JLabel("Bytes IN: ");
		this.bytesINLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.bytesINLabel);
		label1 = new JLabel("Packets IN: ");
		this.packetsINLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.packetsINLabel);
		label1 = new JLabel("Bytes OUT: ");
		this.bytesOUTLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.bytesOUTLabel);
		label1 = new JLabel("Packets OUT: ");
		this.packetsOUTLabel = new JLabel("-");
		panel.add(label1);
		panel.add(this.packetsOUTLabel);
		mainPanel.add(panel);

		//Disk
		panel=new JPanel();
		panel.setLayout(new GridLayout(0,2,5,5));
		panel.setBorder(BorderFactory.createTitledBorder("Disk Stats"));
		label1 = new JLabel("Total Disk Space: ");
		this.diskTotal = new JLabel("-");
		panel.add(label1);
		panel.add(this.diskTotal);
		label1 = new JLabel("Disk Used: ");
		this.diskUsed = new JLabel("-");
		panel.add(label1);
		panel.add(this.diskUsed);
		label1 = new JLabel("Disk Free: ");
		this.diskFree = new JLabel("-");
		panel.add(label1);
		panel.add(this.diskFree);
		mainPanel.add(panel);
		
		//DiskIO
		panel=new JPanel();
		panel.setLayout(new GridLayout(0,2,5,5));
		panel.setBorder(BorderFactory.createTitledBorder("Disk IO Stats"));
		label1 = new JLabel("Reads: ");
		this.readkbps = new JLabel("-");
		panel.add(label1);
		panel.add(this.readkbps);
		label1 = new JLabel("Writes: ");
		this.writekbps = new JLabel("-");
		panel.add(label1);
		panel.add(this.writekbps);
		label1 = new JLabel("IO time: ");
		this.iotime = new JLabel("-");
		panel.add(label1);
		panel.add(this.iotime);
		mainPanel.add(panel);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	//Memory
	public void setTotalMemLabelText(String s){
		this.totalMemLabel.setText(s);
	}
	
	public void setUsedMemLabelText(String s){
		this.usedMemLabel.setText(s);
	}
	
	public void setFreeMemLabelText(String s){
		this.freeMemLabel.setText(s);
	}
	
	public void setCacheMemLabelText(String s){
		this.cacheMemLabel.setText(s);
	}
	
	public void setTotalSwapMemLabelText(String s){
		this.totalSwapMemLabel.setText(s);
	}
	
	public void setFreeSwapMemLabelText(String s){
		this.freeSwapMemLabel.setText(s);
	}
	//
	
	//CPU
	public void setCPUTotalUsageLabelText(String s){
		this.cpuTotalUsageLabel.setText(s);
	}
	
	public void setCPUUserUsageLabelText(String s){
		this.cpuUserUsageLabel.setText(s);
	}
	
	public void setCPUSystemUsageLabelText(String s){
		this.cpuSystemUsageLabel.setText(s);
	}
	
	public void setCPUIdleUsageLabelText(String s){
		this.cpuIdleUsageLabel.setText(s);
	}
	
	public void setCPUIOwaitUsageLabelText(String s){
		this.cpuIOwaitUsageLabel.setText(s);
	}
	//
	
	//Network
	public void setBytesINLabelText(String s){
		this.bytesINLabel.setText(s);
	}
	
	public void setBytesOUTLabelText(String s){
		this.bytesOUTLabel.setText(s);
	}
	
	public void setPacketsINLabelText(String s){
		this.packetsINLabel.setText(s);
	}
	
	public void setPacketsOUTLabelText(String s){
		this.packetsOUTLabel.setText(s);
	}
	//
	
	//Disk
	public void setDiskTotalLabelText(String s){
		this.diskTotal.setText(s);
	}
	
	public void setDiskFreeLabelText(String s){
		this.diskFree.setText(s);
	}
	
	public void setDiskUsedLabelText(String s){
		this.diskUsed.setText(s);
	}
	//
	
	//Disk IO
	public void setReadkbpsLabelText(String s){
		this.readkbps.setText(s);
	}
	
	public void setWritekbpsLabelText(String s){
		this.writekbps.setText(s);
	}
	
	public void setIOtimeText(String s){
		this.iotime.setText(s);
	}
	//
}
