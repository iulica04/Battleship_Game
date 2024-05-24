import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MyBoardUI extends JFrame {
    private static final int GRID_SIZE = 10; // Dimensiunea grilei 10x10
    private JPanel gridPanel;
    private JPanel[][] cells; // Stocăm referințele la celule
    private ShipPanel[] ships;

    public MyBoardUI() {
        setTitle("My Board");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Creăm grila și inițializăm celulele
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

        // Creăm panoul pentru nave
        JPanel shipsPanel = new JPanel();
        shipsPanel.setLayout(new BoxLayout(shipsPanel, BoxLayout.Y_AXIS));

        // Exemplu de creare a 3 nave cu dimensiuni diferite
        ships = new ShipPanel[3];
        ships[0] = new ShipPanel("Ship 1", 3); // Navă de dimensiune 3
        ships[1] = new ShipPanel("Ship 2", 2); // Navă de dimensiune 2
        ships[2] = new ShipPanel("Ship 3", 4); // Navă de dimensiune 4

        for (ShipPanel ship : ships) {
            shipsPanel.add(ship);
            DragSource ds = new DragSource();
            ds.createDefaultDragGestureRecognizer(ship, DnDConstants.ACTION_MOVE, new ShipDragGestureListener());
        }

        add(gridPanel, BorderLayout.CENTER);
        add(shipsPanel, BorderLayout.EAST);

        setLocationRelativeTo(null); // Centrează fereastra pe ecran
        setVisible(true);
    }

    private class ShipPanel extends JPanel {
        private String name;
        private int size; // Dimensiunea navei

        public ShipPanel(String name, int size) {
            this.name = name;
            this.size = size;
            setPreferredSize(new Dimension(100, 50 * size)); // Dimensiunea vizuală a navei
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
                ship.setVisible(false); // Ascunde nava după plasare
            }
        }
    }

    private class ShipDropTargetListener implements DropTargetListener {
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
                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    String data = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                    String[] parts = data.split(":");
                    String shipName = parts[0];
                    int shipSize = Integer.parseInt(parts[1]);

                    JPanel cell = (JPanel) dtde.getDropTargetContext().getComponent();
                    Point location = cell.getLocation();
                    int row = location.y / (gridPanel.getHeight() / GRID_SIZE);
                    int col = location.x / (gridPanel.getWidth() / GRID_SIZE);

                    // Verificăm dacă nava se încadrează pe grilă
                    if (col + shipSize <= GRID_SIZE) {
                        for (int i = 0; i < shipSize; i++) {
                            cells[row][col + i].setBackground(Color.GRAY);
                        }
                        dtde.dropComplete(true);
                    } else {
                        dtde.rejectDrop();
                    }
                } else {
                    dtde.rejectDrop();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MyBoardUI::new);
    }
}
