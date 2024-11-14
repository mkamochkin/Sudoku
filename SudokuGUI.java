import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

public class SudokuGUI extends JFrame {
    private JTextField[][] cells = new JTextField[9][9];
    private Integer[][] sampleBoard = {
        {5, 3, null, null, 7, null, null, null, null},
        {6, null, null, 1, 9, 5, null, null, null},
        {null, 9, 8, null, null, null, null, 6, null},
        {8, null, null, null, 6, null, null, null, 3},
        {4, null, null, 8, null, 3, null, null, 1},
        {7, null, null, null, 2, null, null, null, 6},
        {null, 6, null, null, null, null, 2, 8, null},
        {null, null, null, 4, 1, 9, null, null, 5},
        {null, null, null, null, 8, null, null, 7, 9}
    };

    private Stack<State> boardStack = new Stack<>();
    private Integer[][] board;
    private Timer timer;

    public SudokuGUI() {
        setTitle("Sudoku Solver");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(9, 9));

        // Initialize the grid of JTextFields with sample board values
        board = new Integer[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j] = new JTextField();
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                if (sampleBoard[i][j] != null) {
                    cells[i][j].setText(sampleBoard[i][j].toString());
                    cells[i][j].setEditable(false); // Make predefined values non-editable
                    board[i][j] = sampleBoard[i][j];
                }
                boardPanel.add(cells[i][j]);
            }
        }

        add(boardPanel, BorderLayout.CENTER);

        JButton solveButton = new JButton("Solve Instantly");
        solveButton.addActionListener(e -> {
            Integer[][] solvedBoard = SudokuSolver(sampleBoard);
            if (solvedBoard != null) {
                displaySolution(solvedBoard);
            } else {
                JOptionPane.showMessageDialog(null, "No solution exists");
            }
        });

        JButton stepByStepButton = new JButton("Step-by-Step Solve");
        stepByStepButton.addActionListener(e -> {
            boardStack.clear();
            for (int i = 0; i < 9; i++) {
                System.arraycopy(sampleBoard[i], 0, board[i], 0, 9);
            }
            startStepByStepSolve();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(solveButton);
        buttonPanel.add(stepByStepButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void startStepByStepSolve() {
        timer = new Timer(10, new ActionListener() {
            int row = 0, col = 0;
            boolean isSolving = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSolving) {
                    timer.stop();
                    return;
                }

                if (row < 9 && col < 9) {
                    if (board[row][col] == null) {
                        boolean placed = false;
                        int startVal = 1;

                        if (!boardStack.isEmpty() && boardStack.peek().row == row && boardStack.peek().col == col) {
                            startVal = boardStack.peek().lastTried + 1;
                            boardStack.pop();
                        }

                        for (int num = startVal; num <= 9; num++) {
                            if (canPlace(board, row, col, num)) {
                                board[row][col] = num;
                                boardStack.push(new State(copyBoard(board), row, col, num));
                                cells[row][col].setText(String.valueOf(num));
                                placed = true;
                                break;
                            }
                        }

                        if (!placed) {
                            cells[row][col].setText(""); // Clear cell if backtracking
                            boolean foundPreviousUntried = false;
                            while (!boardStack.isEmpty()) {
                                State prevState = boardStack.pop();
                                board = prevState.board;
                                row = prevState.row;
                                col = prevState.col;
                                int lastTried = prevState.lastTried;

                                if (lastTried < 9) {
                                    board[row][col] = null;
                                    boardStack.push(new State(copyBoard(board), row, col, lastTried));
                                    foundPreviousUntried = true;
                                    break;
                                }
                            }

                            if (!foundPreviousUntried) {
                                JOptionPane.showMessageDialog(null, "No solution exists");
                                isSolving = false;
                            }
                            return;
                        }
                    }

                    if (++col == 9) {
                        col = 0;
                        row++;
                    }
                } else {
                    isSolving = false;
                }
            }
        });

        timer.start();
    }

    private void displaySolution(Integer[][] solvedBoard) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setText(solvedBoard[i][j] != null ? solvedBoard[i][j].toString() : "");
            }
        }
    }

    public static void main(String[] args) {
        SudokuGUI gui = new SudokuGUI();
        gui.setVisible(true);
    }

    public static Integer[][] SudokuSolver(Integer[][] inputBoard) {
        int size = 9;
        Stack<State> boardStack = new Stack<>();
    
        // Initialize board
        Integer[][] board = new Integer[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(inputBoard[i], 0, board[i], 0, size);
        }
    
        int row = 0, col = 0;
        while (row < size && col < size) {
            if (board[row][col] == null) {
                boolean placed = false;
                int startVal = 1;
    
                if (!boardStack.isEmpty() && boardStack.peek().row == row && boardStack.peek().col == col) {
                    startVal = boardStack.peek().lastTried + 1;
                    boardStack.pop();
                }
    
                for (int num = startVal; num <= 9; num++) {
                    if (canPlace(board, row, col, num)) {
                        board[row][col] = num;
                        boardStack.push(new State(copyBoard(board), row, col, num));
                        placed = true;
                        break;
                    }
                }
    
                if (!placed) {
                    boolean foundPreviousUntried = false;
                    while (!boardStack.isEmpty()) {
                        State prevState = boardStack.pop();
                        board = prevState.board;
                        row = prevState.row;
                        col = prevState.col;
                        int lastTried = prevState.lastTried;
    
                        if (lastTried < 9) {
                            board[row][col] = null;
                            boardStack.push(new State(copyBoard(board), row, col, lastTried));
                            foundPreviousUntried = true;
                            break;
                        }
                    }
    
                    if (!foundPreviousUntried) {
                        return null; // No solution exists
                    }
                    continue;
                }
            }
    
            if (++col == size) {
                col = 0;
                row++;
            }
        }
        return board;
    }
    

    static class State {
        Integer[][] board;
        int row;
        int col;
        int lastTried;

        public State(Integer[][] board, int row, int col, int lastTried) {
            this.board = board;
            this.row = row;
            this.col = col;
            this.lastTried = lastTried;
        }
    }

    public static Integer[][] copyBoard(Integer[][] board) {
        Integer[][] copy = new Integer[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, 9);
        }
        return copy;
    }

    public static boolean checkRow(Integer[][] board, int row, int val) {
        for (int i = 0; i < 9; i++) {
            if (board[row][i] != null && board[row][i] == val) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkCol(Integer[][] board, int col, int val) {
        for (int i = 0; i < 9; i++) {
            if (board[i][col] != null && board[i][col] == val) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkSubgrid(Integer[][] board, int row, int col, int val) {
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[boxRow + i][boxCol + j] != null && board[boxRow + i][boxCol + j] == val) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean canPlace(Integer[][] board, int row, int col, int val) {
        return checkCol(board, col, val) && checkRow(board, row, val) && checkSubgrid(board, row, col, val);
    }
}