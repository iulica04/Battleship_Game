import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class MyBoardUI extends JFrame {
    private GameClient client;
    private static final int GRID_SIZE = 10;
    private JPanel gridPanel;
    private JPanel[][] cells;
    private ShipPanel[] ships;
    private int placedShipCount = 0;

    public MyBoardUI(String serverAddress, int serverPort) {
        setTitle("My Board");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        //Creates the grid (MyBoard)
        gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        gridPanel.setPreferredSize(new Dimension(400, 400));
        cells = new JPanel[GRID_SIZE][GRID_SIZE];

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JPanel cell = new JPanel();
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cell.setTransferHandler(new ShipTransferHandler());
                new DropTarget(cell, DnDConstants.ACTION_MOVE, new ShipDropTargetListener(), true);
                cells[i][j] = cell;
                gridPanel.add(cell);
            }
        }

        //Ships Panel
        JPanel shipsPanel = new JPanel();
        shipsPanel.setLayout(new BoxLayout(shipsPanel, BoxLayout.Y_AXIS));

        //Ship types
        ships = new ShipPanel[9];
        ships[0] = new ShipPanel("Ship 1 DOWN", 1);
        ships[1] = new ShipPanel("Ship 2 DOWN", 2);
        ships[2] = new ShipPanel("Ship 3 DOWN", 3);
        ships[3] = new ShipPanel("Ship 4 DOWN", 4);
        ships[4] = new ShipPanel("Ship 5 DOWN", 5);

        ships[5] = new ShipPanel("Ship 2 UP", 2);
        ships[6] = new ShipPanel("Ship 3 UP", 3);
        ships[7] = new ShipPanel("Ship 4 UP", 4);
        ships[8] = new ShipPanel("Ship 5 UP", 5);

        for (ShipPanel ship : ships) {
            shipsPanel.add(ship);
            DragSource ds = new DragSource();
            ds.createDefaultDragGestureRecognizer(ship, DnDConstants.ACTION_MOVE, new ShipDragGestureListener());
        }

        add(gridPanel, BorderLayout.CENTER);
        add(shipsPanel, BorderLayout.EAST);

        setLocationRelativeTo(null); //for centering the window !!!!
        setVisible(true);

        // server connection
        try {
            client = new GameClient(serverAddress, serverPort, this::handleServerResponse);
            client.connect();
        } catch (IOException e) {
            System.out.println("Could not connect to server: " + e.getMessage());
        }
    }

    private class ShipPanel extends JPanel {
        private String name;
        private int size;

        public ShipPanel(String name, int size) {
            this.name = name;
            this.size = size;
            if(name.endsWith("DOWN")) {
                setPreferredSize(new Dimension(150 , 50*size));
            } else {
                setPreferredSize(new Dimension(50*size, 150));
            }
            setBackground(Color.GRAY);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            JLabel label = new JLabel(name);
            add(label);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    JComponent comp = (JComponent) e.getSource();
                    TransferHandler handler = comp.getTransferHandler();
                    handler.exportAsDrag(comp, e, TransferHandler.MOVE);
                }
            });

            setTransferHandler(new ShipTransferHandler());
        }

        public String getName() {
            return name;
        }

        public int getShipSize() {
            return size;
        }
    }

    private class ShipDragGestureListener implements DragGestureListener {
        @Override
        public void dragGestureRecognized(DragGestureEvent dge) {
            Cursor cursor = null;
            ShipPanel ship = (ShipPanel) dge.getComponent();
            StringSelection text = new StringSelection(ship.getName() + ":" + ship.getShipSize());
            dge.startDrag(cursor, text, new ShipDragSourceListener());
        }
    }

    private class ShipDragSourceListener implements DragSourceListener {
        @Override
        public void dragEnter(DragSourceDragEvent dsde) {}

        @Override
        public void dragOver(DragSourceDragEvent dsde) {}

        @Override
        public void dropActionChanged(DragSourceDragEvent dsde) {}

        @Override
        public void dragExit(DragSourceEvent dse) {}

        @Override
        public void dragDropEnd(DragSourceDropEvent dsde) {
            if (dsde.getDropSuccess()) {
                ShipPanel ship = (ShipPanel) dsde.getDragSourceContext().getComponent();
                ship.setBackground(Color.ORANGE);
                ship.setVisible(false);

            }
        }
    }

    private class ShipDropTargetListener extends Component implements DropTargetListener {
        @Override
        public void dragEnter(DropTargetDragEvent dtde) {}

        @Override
        public void dragOver(DropTargetDragEvent dtde) {}

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {}

        @Override
        public void dragExit(DropTargetEvent dte) {}


        @Override
        public void drop(DropTargetDropEvent dtde) {
            try {
                Transferable transferable = dtde.getTransferable();
                if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    String data = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                    String[] parts = data.split(":");
                    String shipName = parts[0];
                    int shipSize = Integer.parseInt(parts[1]);

                    JPanel cell = (JPanel) dtde.getDropTargetContext().getComponent();
                    Point location = cell.getLocation();
                    int row = location.y / (gridPanel.getHeight() / GRID_SIZE);
                    int col = location.x / (gridPanel.getWidth() / GRID_SIZE);

                    //Checks if all ships have been placed
                    if (placedShipCount >= 5) {
                        checkGameStatus();
                        return;
                    }

                    // Checks if the ship can be placed
                    if(placedShipCount <= 5){
                    if (row + shipSize <= GRID_SIZE && shipName.endsWith("UP")) {

                            for (int i = 0; i < shipSize; i++) {
                                cells[row + i][col].setBackground(Color.GRAY);
                            }

                            String shipPosition = String.format("%d %d %d %d", row, col, row + shipSize - 1, col);
                            System.out.println("Ship position: " + shipPosition);
                            sendCommandSetPositions(row, col, row + shipSize - 1, col, shipName);
                            placedShipCount++;

                            dtde.dropComplete(true);

                        } else if(col + shipSize <= GRID_SIZE && shipName.endsWith("DOWN")) {
                            for (int i = 0; i < shipSize; i++) {
                            cells[row][col + i].setBackground(Color.GRAY);
                             }

                        String shipPosition = String.format("%d %d %d %d", row, col, row, col + shipSize - 1);
                        System.out.println("Ship position: " + shipPosition);
                        sendCommandSetPositions(row, col, row, col + shipSize - 1, shipName);
                        placedShipCount++;

                        dtde.dropComplete(true);
                    }
                    }else {
                        dtde.rejectDrop();
                    }
                }
            } catch (Exception e) {
                dtde.rejectDrop();
                e.printStackTrace();
            }
        }

    }

    private class ShipTransferHandler extends TransferHandler {
        @Override
        protected Transferable createTransferable(JComponent c) {
            ShipPanel ship = (ShipPanel) c;
            return new StringSelection(ship.getName() + ":" + ship.getShipSize());
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }
    }
    private void checkGameStatus() {
        if (placedShipCount >= 5) {
            System.out.println("All ships placed!You may now play the game!");
            JOptionPane.showMessageDialog(this, "All ships placed!");
            placedShipCount=0;

        } else {
            System.out.println("Not all ships placed!");
            JOptionPane.showMessageDialog(this, "Not all ships placed!");

        }
    }
    private void sendCommandSetPositions(int x1, int y1, int x2, int y2, String shipName) {
            String command = "place ship " + x1 + " " + y1 + " " + x2 + " " + y2;
            System.out.println("Sending move: " + command);
            client.sendCommand(command);
    }
    private void handleServerResponse(String response) {
        SwingUtilities.invokeLater(() -> {  //responsive UI

            System.out.println("Received response from server: " + response);

            if (response.startsWith("Enter your name")) {
                String playerName = JOptionPane.showInputDialog(this, "Enter your name:");
                if (playerName != null && !playerName.trim().isEmpty())
                    client.sendCommand("name " + playerName.trim());
            }else if(response.startsWith("Ship placed.")){
                System.out.println("Ship placed.");
            }
        });
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WelcomeScreen());
    }
}
