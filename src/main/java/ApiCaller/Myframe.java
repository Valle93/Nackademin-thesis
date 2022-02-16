package ApiCaller;

import Entities.Brand;
import Entities.Location;
import Entities.Product;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

public class Myframe extends JFrame {

    private String service;
    private int callsPerThread;
    private int nr_of_threads;
    private int offset;

    boolean canWeClickButton;
    private Color placeholderColor;
    private ApiCallButton placeholderButton;
    private long totalTime;
    private int[] millis0to200;
    private int[] millis0to50;
    private List<ApiCall> allCalls;
    private TextArea textArea;
    private JPanel rightPanel;
    private TextArea botLeftArea;

    public Myframe(int root_width, int root_height){

        setMinimumSize(new Dimension(1200, 800));

        service = "not set";
        callsPerThread = 0;
        nr_of_threads = 0;
        offset = 0;

        millis0to50 = new int[5];
        millis0to200 = new int[5];

        canWeClickButton = true;

        JPanel root = new JPanel();
        root.setBackground(Color.white);
        root.setBounds(0,0,root_width,root_height);
        root.setLayout(null);

        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.cyan);
        leftPanel.setBounds(0,0,root_width/2 - 20, root_height);

        GridLayout gridForLeftPanel = new GridLayout(2, 1);
        leftPanel.setLayout(gridForLeftPanel);

        JPanel topLeftPanel = new JPanel();
        textArea = new TextArea();
        textArea.setBounds(0, 0, 400, 400);
        topLeftPanel.add(textArea);

        writeAllTheData();

        leftPanel.add(topLeftPanel);

        root.add(leftPanel);

        JPanel bottomLeftPanel = new JPanel();
        bottomLeftPanel.setBackground(Color.yellow);
        botLeftArea = new TextArea();
        bottomLeftPanel.add(botLeftArea);

        JButton runApiTest = new JButton("Run ApiTest");

        runApiTest.addActionListener(e -> {

            if(!canWeClickButton){

                return;
            }

            canWeClickButton = false;

            try {

                instantiateAnApiCallerstation();

            } catch (IOException ioException) {
                ioException.printStackTrace();

            }

            canWeClickButton = true;

        });

        JButton openApiSettings = new JButton("Settings");

        openApiSettings.addActionListener(e -> {

            if(!canWeClickButton){

                return;
            }

               JFrame dialog = new JFrame();
               dialog.setSize(300,300);
               dialog.setVisible(true);

               JPanel content = new JPanel(new GridLayout(0,2));

               JLabel[] labels = new JLabel[]{new JLabel("api"), new JLabel("nr.of.threads"),
                       new JLabel("calls.per.thread"), new JLabel("offset")};

               JTextField[] fields = new JTextField[4];

               for (int i = 0; i < 4; i++) {
                    fields[i] = new JTextField("", 30);
                    content.add(labels[i]);
                    content.add(fields[i]);
                }

               JButton apply = new JButton("Apply");

               apply.addActionListener(d -> {

                   System.out.println("hola");

                   service = fields[0].getText();
                   nr_of_threads = Integer.parseInt(fields[1].getText());
                   callsPerThread = Integer.parseInt(fields[2].getText());
                   offset = Integer.parseInt(fields[3].getText());

                   writeAllTheData();

                   dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));

               });

               content.add(apply);

               dialog.add(content);

        });

        bottomLeftPanel.add(runApiTest);
        bottomLeftPanel.add(openApiSettings);

        leftPanel.add(bottomLeftPanel);

        rightPanel = new JPanel();
        rightPanel.setBackground(Color.white);

        GridLayout grid = new GridLayout(0, 5, 0,0);

        rightPanel.setLayout(grid);

        JScrollPane scroll = new JScrollPane(rightPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        scroll.setBounds(root_width/2, 5, root_width/2 - 20, 800);

        root.add(scroll);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setSize(1200, 800);
        setResizable(false);
        add(root);

        setVisible(true);
    }

    Color getColorFromTimeInMillis(Long millis){

        if(millis <= 10){

            return new Color(0, 150, 255);
        }
        if(millis <= 20){

            return new Color(0 , 200, 200);
        }
        if(millis <= 30){

            return new Color(100, 255, 100);
        }

        if(millis <= 40){

            return new Color(255, 255, 0);
        }

        if(millis <= 50){

            return new Color(255, 160, 0);
        }

        return new Color(160, 0, 0);

    }

    String getInfoFromPanel(ApiCallButton panel){

        ApiCall inQuestion = panel.getApiCall();

        String toReturn = "url = " + panel.getApiCall().getApiCall_url() + "\n\n";

        toReturn += "Apicall : \n\n";

        toReturn += "Price min: " + inQuestion.getAdvancedQueryCall().getPrice_min();

        toReturn += "\nprice max: " + inQuestion.getAdvancedQueryCall().getPrice_max();

        toReturn += "\n\n Locations : ";

        for(Location l : inQuestion.getAdvancedQueryCall().getLocations()){

            toReturn += l.getName() + ", ";
        }

        toReturn += "\n\n Brands : ";

        for(Brand b : inQuestion.getAdvancedQueryCall().getBrands()){

            toReturn += b.getName() + ", ";
        }

        toReturn += "\n\n Products : \n\n amount : " + inQuestion.getProducts().size() + "\n\n";

        for(Product p : inQuestion.getProducts()){

            toReturn += p.getName() + "  :  id = " + p.getMysql_id() + "\n\n";
        }

        return toReturn;
    }

    void instantiateAnApiCallerstation() throws IOException {

        this.rightPanel.removeAll();

        long then = System.currentTimeMillis();

        ApiCallerStation apiCallerStation = new ApiCallerStation(service, nr_of_threads,
                callsPerThread, offset, Call.AdvancedQuery);

        do {

            // Do nothing

        } while (apiCallerStation.getAreTheThreadsDone().get() == false);

        totalTime = System.currentTimeMillis() - then;

        allCalls = apiCallerStation.getAllApiCallsDone();

        writeAllTheData();

        for(ApiCall call : allCalls){

            ApiCallButton panel = new ApiCallButton(call);

            panel.setBackground(getColorFromTimeInMillis(call.getTimeInMillis()));

            JLabel label = new JLabel(String.valueOf(call.getTimeInMillis()));

            panel.add(label);

            panel.addActionListener(e -> {

                if(this.placeholderButton != null)
                    this.placeholderButton.setBackground(placeholderColor);

                // Paint the old panel into its old color

                this.placeholderButton = panel;

                this.placeholderColor = panel.getBackground();

                panel.setBackground(Color.white);

                // send info into bottomLeftPanel

                botLeftArea.setText(getInfoFromPanel(panel));

            });

            this.rightPanel.add(panel);

        }

        this.millis0to50 = apiCallerStation.getMillis0to50();
        this.millis0to200 = apiCallerStation.getMillis0to200();
        writeResults();

        this.pack();
        this.repaint();

    }

    void writeAllTheData(){

        textArea.setText("");

        textArea.append("The operation took : " + totalTime + " milliseconds\n\n");

        textArea.append("service used : " + service + "\n");
        textArea.append("Nr of api callers : " + nr_of_threads + "\n");
        textArea.append("calls per thread : " + callsPerThread + "\n");
        textArea.append("offset : " + offset + "\n\n");


    }

    void writeResults(){

        textArea.append("Calls, milliseconds to amount\n\n");

        for (int i = 0; i < millis0to200.length; i++) {

            textArea.append(" " + 50 * i + " - " + (50 * (i + 1)) + " : " + millis0to200[i] + "\n" );
        }

        textArea.append("Calls, sub 50 milliseconds\n\n");

        for (int i = 0; i < millis0to50.length; i++) {

            textArea.append(" " + 10 * i + " - " + (10 * (i + 1)) + " : " + millis0to50[i] + " \n");
        }
    };

}
