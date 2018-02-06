package spb;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.swing.JFrame;
import javax.swing.JButton;

import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.statistics.HistogramDataset;

import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jade.core.AID;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Frame1 {

	private JFrame frame;
	private JPanel panel1;
	private JPanel panel2;
	private JPanel panel3;
	private JPanel panel4;
	private JPanel panel5;
	private JButton btn1;
	private Map<AID, Estacao> estacoes;
	private int numUtilizadores;

	public Frame1(Map<AID, Estacao> e, Map<LocalTime, Integer> time, int num) {
		numUtilizadores = num;
		initialize();
		frame.setVisible(true);
		estacoes = new HashMap<AID, Estacao>();
		for (Map.Entry<AID, Estacao> c : e.entrySet()) {
			estacoes.put(c.getKey(), c.getValue());
		}
		barChart();
		actualiza(time);
		total();
		descontos();
	}

	public void atualizaJanela(Map<AID, Estacao> e, Map<LocalTime, Integer> time, int num) {
		estacoes = new HashMap<AID, Estacao>();
		numUtilizadores = num;
		for (Map.Entry<AID, Estacao> c : e.entrySet()) {
			estacoes.put(c.getKey(), c.getValue());
		}
		barChart();
		actualiza(time);
		total();
		descontos();
	}

	public void descontos() {

		int i = 1;
		DefaultCategoryDataset data = new DefaultCategoryDataset();
		for (Map.Entry<AID, Estacao> c : estacoes.entrySet()) {
			data.addValue(c.getValue().getDescontosAceites(), "", "Estacao" + i);
			i++;
		}

		JFreeChart chart = ChartFactory.createBarChart("Descontos", "", "Quantidade", data, PlotOrientation.HORIZONTAL,
				false, true, true);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		// plot.setDomainGridlinesVisible(true);
		plot.setBackgroundPaint(SystemColor.inactiveCaption);// change background color

		// set bar chart color

		((BarRenderer) plot.getRenderer()).setBarPainter(new StandardBarPainter());

		BarRenderer r = (BarRenderer) chart.getCategoryPlot().getRenderer();
		r.setSeriesPaint(0, Color.YELLOW);

		ChartPanel panel = new ChartPanel(chart);
		panel5.removeAll();
		panel5.add(panel, BorderLayout.CENTER);
		panel5.validate();

	}

	public void total() {
		int total = 0;// total bicicletas em uso
		int total1 = 0;// total bicicletas em repouso
		int total2 = 60;// total bicicletas

		for (Map.Entry<AID, Estacao> c : estacoes.entrySet()) {
			total1 += c.getValue().getnBicicletas();
		}

		total = numUtilizadores;

		DefaultCategoryDataset data = new DefaultCategoryDataset();
		data.addValue(total1, "Nº Bicicletas em repouso", "");
		data.addValue(total, "Nº Bicicletas em uso", "");
		data.addValue(total2, "Total bicicletas", "");

		JFreeChart chart = ChartFactory.createBarChart("Metricas", "", "Numero", data, PlotOrientation.VERTICAL, true,
				true, true);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setDomainGridlinesVisible(true);
		ChartPanel panel = new ChartPanel(chart);
		panel4.removeAll();
		panel4.add(panel, BorderLayout.CENTER);
		panel4.validate();

	}

	public void actualiza(Map<LocalTime, Integer> time) {

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Entry<LocalTime, Integer> c : time.entrySet()) {
			dataset.addValue(c.getValue(), "", c.getKey());
		}

		JFreeChart chart = ChartFactory.createBarChart("Número de Utilizadores no Sistema", "Tempo", "Número", dataset,
				PlotOrientation.VERTICAL, false, true, true);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		// plot.setDomainGridlinesVisible(true);
		plot.setBackgroundPaint(SystemColor.inactiveCaption);// change background color

		// set bar chart color

		((BarRenderer) plot.getRenderer()).setBarPainter(new StandardBarPainter());

		BarRenderer r = (BarRenderer) chart.getCategoryPlot().getRenderer();
		r.setSeriesPaint(0, Color.blue);

		ChartPanel panel = new ChartPanel(chart);
		panel3.removeAll();
		panel3.add(panel, BorderLayout.CENTER);
		panel3.validate();

	}

	public void barChart() {

		DefaultCategoryDataset data = new DefaultCategoryDataset();
		DefaultCategoryDataset data1 = new DefaultCategoryDataset();
		int i = 1;

		for (Map.Entry<AID, Estacao> c : estacoes.entrySet()) {
			int value1 = c.getValue().getnBicicletas();
			// System.out.println(Integer.toString(value1));
			data.addValue(c.getValue().getnBicicletas(), "", "Estacao" + i);
			i++;
		}
		i = 1;
		for (Map.Entry<AID, Estacao> c : estacoes.entrySet()) {
			double value;
			if (c.getValue().getCapacidade() != 0) {
				value = ((double) c.getValue().getnBicicletas() / c.getValue().getCapacidade());
			} else {
				value = 0;
			}
			// System.out.println(Double.toString(value));
			data1.addValue(value * 100, "", "Estacao" + i);
			i++;
		}

		JFreeChart chart = ChartFactory.createBarChart("Numero de Bicicletas por estação", "Estação",
				"Quantidade de bicicletas", data, PlotOrientation.HORIZONTAL, false, true, true);

		JFreeChart chart1 = ChartFactory.createBarChart("Percentagem de bicicletas por estação", "Estação",
				"Quantidade por Capacidade Total(%)", data1, PlotOrientation.HORIZONTAL, false, true, true);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setDomainGridlinesVisible(true);
		ChartPanel panel11 = new ChartPanel(chart);
		ChartPanel panel12 = new ChartPanel(chart1);

		panel1.removeAll();
		panel2.removeAll();
		panel1.add(panel11, BorderLayout.CENTER);
		panel2.add(panel12, BorderLayout.CENTER);
		panel1.validate();
		panel2.validate();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame("Interface");
		frame.setBounds(100, 100, 1222, 538);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		panel1 = new JPanel();
		panel1.setBounds(10, 11, 380, 266);
		frame.getContentPane().add(panel1);
		panel1.setLayout(new BorderLayout(0, 0));

		panel2 = new JPanel();
		panel2.setBounds(431, 11, 405, 266);
		frame.getContentPane().add(panel2);
		panel2.setLayout(new BorderLayout(0, 0));

		panel3 = new JPanel();
		panel3.setBounds(431, 288, 405, 204);
		frame.getContentPane().add(panel3);
		panel3.setLayout(new BorderLayout(0, 0));

		panel4 = new JPanel();
		panel4.setBounds(10, 288, 380, 204);
		frame.getContentPane().add(panel4);
		panel4.setLayout(new BorderLayout(0, 0));

		btn1 = new JButton("Esta\u00E7ao 1");
		btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = 1;
				int j = 1;
				Estacao est = new Estacao();

				if (i <= estacoes.size()) {
					for (Map.Entry<AID, Estacao> c : estacoes.entrySet()) {
						if (j == i) {
							est = c.getValue().clone();
							break;
						} else
							j++;
					}
					JOptionPane.showMessageDialog(frame,
							"Nome: " + "Estacao1" + "\n" + "Coordenadas: " + est.getCoordenadas() + "\n"
									+ "Nº Bicicletas: " + est.getnBicicletas() + "\n" + "Capacidade Maxima: "
									+ est.getCapacidade() + "\n" + "Descontos Aceites: " + est.getDescontosAceites()
									+ "\n" + "Descontos Recusados: " + est.getDescontosRecusados());
				}
			}
		});
		btn1.setBounds(961, 28, 112, 31);
		frame.getContentPane().add(btn1);

		JButton btn2 = new JButton("Esta\u00E7ao 2");
		btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = 2;
				int j = 1;
				Estacao est = new Estacao();

				if (i <= estacoes.size()) {
					for (Map.Entry<AID, Estacao> c : estacoes.entrySet()) {
						if (j == i) {
							est = c.getValue().clone();
							break;
						} else
							j++;
					}
					JOptionPane.showMessageDialog(frame,
							"Nome: " + "Estacao2" + "\n" + "Coordenadas: " + est.getCoordenadas() + "\n"
									+ "Nº Bicicletas: " + est.getnBicicletas() + "\n" + "Capacidade Maxima: "
									+ est.getCapacidade() + "\n" + "Descontos Aceites: " + est.getDescontosAceites()
									+ "\n" + "Descontos Recusados: " + est.getDescontosRecusados());
				}
			}
		});
		btn2.setBounds(961, 70, 112, 31);
		frame.getContentPane().add(btn2);

		JButton btn3 = new JButton("Esta\u00E7ao 3");
		btn3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = 3;
				int j = 1;
				Estacao est = new Estacao();

				if (i <= estacoes.size()) {
					for (Map.Entry<AID, Estacao> c : estacoes.entrySet()) {
						if (j == i)
							est = c.getValue().clone();
						else
							j++;
					}
					JOptionPane.showMessageDialog(frame,
							"Nome: " + "Estacao3" + "\n" + "Coordenadas: " + est.getCoordenadas() + "\n"
									+ "Nº Bicicletas: " + est.getnBicicletas() + "\n" + "Capacidade Maxima: "
									+ est.getCapacidade() + "\n" + "Descontos Aceites: " + est.getDescontosAceites()
									+ "\n" + "Descontos Recusados: " + est.getDescontosRecusados());
				}
			}
		});
		btn3.setBounds(961, 112, 112, 31);
		frame.getContentPane().add(btn3);

		JButton btn4 = new JButton("Esta\u00E7ao 4");
		btn4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = 4;
				int j = 1;
				Estacao est = new Estacao();

				if (i <= estacoes.size()) {
					for (Map.Entry<AID, Estacao> c : estacoes.entrySet()) {
						if (j == i)
							est = c.getValue().clone();
						else
							j++;
					}
					JOptionPane.showMessageDialog(frame,
							"Nome: " + "Estacao4" + "\n" + "Coordenadas: " + est.getCoordenadas() + "\n"
									+ "Nº Bicicletas: " + est.getnBicicletas() + "\n" + "Capacidade Maxima: "
									+ est.getCapacidade() + "\n" + "Descontos Aceites: " + est.getDescontosAceites()
									+ "\n" + "Descontos Recusados: " + est.getDescontosRecusados());
				}
			}
		});
		btn4.setBounds(961, 154, 112, 31);
		frame.getContentPane().add(btn4);

		JButton btn5 = new JButton("Esta\u00E7ao 5");
		btn5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = 5;
				int j = 1;
				Estacao est = new Estacao();

				if (i <= estacoes.size()) {
					for (Map.Entry<AID, Estacao> c : estacoes.entrySet()) {
						if (j == i)
							est = c.getValue().clone();
						else
							j++;
					}
					JOptionPane.showMessageDialog(frame,
							"Nome: " + "Estacao5" + "\n" + "Coordenadas: " + est.getCoordenadas() + "\n"
									+ "Nº Bicicletas: " + est.getnBicicletas() + "\n" + "Capacidade Maxima: "
									+ est.getCapacidade() + "\n" + "Descontos Aceites: " + est.getDescontosAceites()
									+ "\n" + "Descontos Recusados: " + est.getDescontosRecusados());
				}
			}
		});
		btn5.setBounds(961, 196, 112, 31);
		frame.getContentPane().add(btn5);

		panel5 = new JPanel();
		panel5.setBounds(853, 288, 347, 204);
		frame.getContentPane().add(panel5);
		panel5.setLayout(new BorderLayout(0, 0));
	}
}
