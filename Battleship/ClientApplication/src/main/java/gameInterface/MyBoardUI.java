package gameInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import client.GameClient;

public class MyBoardUI extends JFrame {
    private GameClient client;
    private static final int GRID_SIZE = 10;
    private static final int CELL_SIZE = 30; // Dimensiunea fiecărei celule
    private JPanel gridPanel;
    private JPanel[][] cells;
    private ShipPanel[] ships;
    private int placedShipCount = 0;

    public MyBoardUI(GameClient client) {
        this.client = client;

        setTitle("My Board");
        setSize(700, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Creates the grid (MyBoard)
        gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        gridPanel.setSize(new Dimension(CELL_SIZE * GRID_SIZE, CELL_SIZE * GRID_SIZE));
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        gridPanel.setBackground(Color.LIGHT_GRAY);
        cells = new JPanel[GRID_SIZE][GRID_SIZE];

        // După crearea gridului (MyBoard) și a adăugării celulelor în gridPanel
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JPanel cell = new JPanel();
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cell.setTransferHandler(new ShipTransferHandler());
                cell.setBackground(Color.CYAN);
                new DropTarget(cell, DnDConstants.ACTION_MOVE, new ShipDropTargetListener(), true);
                cells[i][j] = cell;
                gridPanel.add(cell);
            }
        }

        // Ships Panel
        JPanel shipsPanel = new JPanel();
        shipsPanel.setLayout(new BoxLayout(shipsPanel, BoxLayout.Y_AXIS));
        shipsPanel.add(new JLabel("DRAG THE SHIPS"));
        // Ship types
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

        //Buttons Panel (Edit)
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

        buttonsPanel.add(new JLabel("  Edit your board:  "));
        JButton removeShipButton = new JButton("Remove Ship");
        JButton startButton = new JButton("Start Game");

        buttonsPanel.add(removeShipButton);
        buttonsPanel.add(new JLabel("      If you are ready to play, press:  "));
        buttonsPanel.add(startButton);

        //MAIN PANEL
        add(gridPanel, BorderLayout.CENTER);
        add(shipsPanel, BorderLayout.EAST);
        add(buttonsPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null); // for centering the window
        setVisible(true);

        // Add action listener to the remove ship button
        removeShipButton.addActionListener(e -> {
            // Acțiunea de ștergere a unei singure bărci
            String message = "Select a cell from the ship you want to remove.";
            JOptionPane.showMessageDialog(MyBoardUI.this, message);

            // Adăugăm un mouse listener pentru fiecare celulă pentru a permite utilizatorului să selecteze o celulă
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    JPanel cell = cells[i][j];
                    cell.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            // Coordonatele celulei
                            int row = cell.getParent().getComponentZOrder(cell) / GRID_SIZE;
                            int col = cell.getParent().getComponentZOrder(cell) % GRID_SIZE;
                            // Verificăm dacă celula face parte dintr-o navă
                            if (cellHasShip(row, col) && placedShipCount > 0) {
                                // Trimitem comanda de ștergere a navei la server
                                clearShipCells(row, col);
                                placedShipCount--;
                                // Dupa ce am sters barca, eliminam listenerul de mouse de pe celule pentru a se face doar o stergere
                                removeMouseListeners();
                            } else {
                                // Afișăm un mesaj că celula selectată nu este parte a unei nave
                                JOptionPane.showMessageDialog(MyBoardUI.this, "This cell is not part of a ship.");
                            }
                        }
                    });
                }
            }
        });


        // server connection

    }

    // Metoda pentru a elimina toți listenerii de mouse de pe celule după ce o barcă a fost ștearsă
    private void removeMouseListeners() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JPanel cell = cells[i][j];
                MouseListener[] listeners = cell.getMouseListeners();
                for (MouseListener listener : listeners) {
                    cell.removeMouseListener(listener);
                }
            }
        }
    }

    // Metoda pentru a verifica dacă o celulă face parte dintr-o navă
    private boolean cellHasShip(int row, int col) {
        // Verificăm dacă celula este colorată cu altă culoare decât CYAN
        return !cells[row][col].getBackground().equals(Color.CYAN);

    }

    // Metoda pentru a șterge o navă și a recolora celulele implicate
    private void clearShipCells(int x, int y) {
        // Identificăm dimensiunea și orientarea navei pe baza culorii celulei de start (x, y)
        Color shipColor = cells[x][y].getBackground();
        int rowStart = x;
        int colStart = y;
        int rowEnd = x;
        int colEnd = y;
        if (shipColor.equals(Color.GRAY)) {
            // Nava este orientată vertical (sus-jos)
            while (rowEnd + 1 < GRID_SIZE && cells[rowEnd + 1][colStart].getBackground().equals(Color.GRAY)) {
                rowEnd++;
            }
        } else if (shipColor.equals(Color.LIGHT_GRAY)) {
            // Nava este orientată orizontal (stânga-dreapta)
            while (colEnd + 1 < GRID_SIZE && cells[rowStart][colEnd + 1].getBackground().equals(Color.LIGHT_GRAY)) {
                colEnd++;
            }
        }
        // Trimitem comanda de ștergere a navei la server
        sendCommandRemoveShip(rowStart, colStart, rowEnd, colEnd);
        // Recolorăm celulele navei cu culoarea CYAN
        for (int i = rowStart; i <= rowEnd; i++) {
            for (int j = colStart; j <= colEnd; j++) {
                cells[i][j].setBackground(Color.CYAN);
            }
        }
    }

    private void sendCommandRemoveShip(int x1, int y1, int x2, int y2) {
        String command = "delete ship " + x1 + " " + y1 + " " + x2 + " " + y2;
        System.out.println("Sending command: " + command);
        client.sendCommand(command);
    }


    private class ShipPanel extends JPanel {
        private String name;
        private int size;
        private final int SQUARE_SIZE = 50; // Dimensiunea fiecărui pătrat

        public ShipPanel(String name, int size) {
            this.name = name;
            this.size = size;

            setPreferredSize(new Dimension(260, 60));

            setBackground(Color.LIGHT_GRAY);
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

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            if (size == 1) {
                int x = 90;
                int y = 20;
                for (int i = 0; i < size; i++) {
                    g.setColor(Color.BLACK); // Setare culoare margine
                    g.drawRect(x, y, SQUARE_SIZE, SQUARE_SIZE); // Desenare margine
                    g.setColor(Color.GRAY); // Setare culoare pentru interior
                    g.fillRect(x + 1, y + 1, SQUARE_SIZE - 1, SQUARE_SIZE - 1); // Desenare interior

                    x += SQUARE_SIZE;
                }

            } else if (size == 2) {
                int x = 70;
                int y = 20;
                for (int i = 0; i < size; i++) {
                    g.setColor(Color.BLACK); // Setare culoare margine
                    g.drawRect(x, y, SQUARE_SIZE, SQUARE_SIZE); // Desenare margine
                    g.setColor(Color.GRAY); // Setare culoare pentru interior
                    g.fillRect(x + 1, y + 1, SQUARE_SIZE - 1, SQUARE_SIZE - 1); // Desenare interior
                    x += SQUARE_SIZE;
                }

            } else if (size == 3) {
                int x = 40;
                int y = 20;
                for (int i = 0; i < size; i++) {
                    g.setColor(Color.BLACK); // Setare culoare margine
                    g.drawRect(x, y, SQUARE_SIZE, SQUARE_SIZE); // Desenare margine
                    g.setColor(Color.GRAY); // Setare culoare pentru interior
                    g.fillRect(x + 1, y + 1, SQUARE_SIZE - 1, SQUARE_SIZE - 1); // Desenare interior
                    x += SQUARE_SIZE;
                }

            } else if (size == 4) {
                int x = 25;
                int y = 20;
                for (int i = 0; i < size; i++) {
                    g.setColor(Color.BLACK); // Setare culoare margine
                    g.drawRect(x, y, SQUARE_SIZE, SQUARE_SIZE); // Desenare margine
                    g.setColor(Color.GRAY); // Setare culoare pentru interior
                    g.fillRect(x + 1, y + 1, SQUARE_SIZE - 1, SQUARE_SIZE - 1); // Desenare interior
                    x += SQUARE_SIZE;
                }

            } else if (size == 5) {
                int x = 5;
                int y = 20;
                for (int i = 0; i < size; i++) {
                    g.setColor(Color.BLACK); // Setare culoare margine
                    g.drawRect(x, y, SQUARE_SIZE, SQUARE_SIZE); // Desenare margine
                    g.setColor(Color.GRAY); // Setare culoare pentru interior
                    g.fillRect(x + 1, y + 1, SQUARE_SIZE - 1, SQUARE_SIZE - 1); // Desenare interior
                    x += SQUARE_SIZE;
                }

            }
            g2d.dispose();
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
        public void dragEnter(DragSourceDragEvent dsde) {
        }

        @Override
        public void dragOver(DragSourceDragEvent dsde) {
        }

        @Override
        public void dropActionChanged(DragSourceDragEvent dsde) {
        }

        @Override
        public void dragExit(DragSourceEvent dse) {
        }

        @Override
        public void dragDropEnd(DragSourceDropEvent dsde) {
            if (dsde.getDropSuccess()) {
                ShipPanel ship = (ShipPanel) dsde.getDragSourceContext().getComponent();
                ship.setVisible(false);
            }
        }
    }

    private class ShipDropTargetListener extends Component implements DropTargetListener {
        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
        }

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

                    // Checks if all ships have been placed
                    if (placedShipCount >= 5) {
                        checkGameStatus();
                        return;
                    }

                    // Checks if the ship can be placed
                    if (placedShipCount <= 5) {
                        if (row + shipSize <= GRID_SIZE && shipName.endsWith("UP")) {

                            for (int i = 0; i < shipSize; i++) {
                                cells[row + i][col].setBackground(Color.GRAY);
                            }

                            String shipPosition = String.format("%d %d %d %d", row, col, row + shipSize - 1, col);
                            System.out.println("Ship position: " + shipPosition);
                            sendCommandSetPositions(row, col, row + shipSize - 1, col, shipName);
                            placedShipCount++;

                            dtde.dropComplete(true);

                        } else if (col + shipSize <= GRID_SIZE && shipName.endsWith("DOWN")) {
                            for (int i = 0; i < shipSize; i++) {
                                cells[row][col + i].setBackground(Color.LIGHT_GRAY);
                            }

                            String shipPosition = String.format("%d %d %d %d", row, col, row, col + shipSize - 1);
                            System.out.println("Ship position: " + shipPosition);
                            sendCommandSetPositions(row, col, row, col + shipSize - 1, shipName);
                            placedShipCount++;

                            dtde.dropComplete(true);
                        }
                    } else {
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
            System.out.println("All ships placed! You may now play the game!");
            JOptionPane.showMessageDialog(this, "All ships placed!");
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
        SwingUtilities.invokeLater(() -> { // responsive UI
            System.out.println("Received response from server: " + response);

            if (response.startsWith("Enter your name")) {
                String playerName = JOptionPane.showInputDialog(this, "Enter your name:");
                if (playerName != null && !playerName.trim().isEmpty())
                    client.sendCommand("name " + playerName.trim());
            } else if (response.startsWith("Ship placed.")) {
                System.out.println("Ship placed.");
            }
        });
    }

}