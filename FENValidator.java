import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FENValidator {

    private static final int BOARD_SIZE = 8;
    private static char[][] boardState;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String fen = "";
        boolean running = true;

        while (running) {
            System.out.println("Enter FEN string (or 'exit' to quit):");
            fen = scanner.nextLine();

            if (fen.equalsIgnoreCase("exit")) {
                running = false;
                continue;
            }

            boardState = parseFEN(fen);

            System.out.println("Enter move in format 'startRow startCol endRow endCol' (e.g., '1 0 2 0'):");
            String moveInput = scanner.nextLine();

            if (moveInput.equalsIgnoreCase("exit")) {
                running = false;
                continue;
            }

            String[] moveParts = moveInput.split(" ");
            if (moveParts.length != 4) {
                System.out.println("Invalid move format. Please try again.");
                continue;
            }

            try {
                int startRow = Integer.parseInt(moveParts[0]);
                int startCol = Integer.parseInt(moveParts[1]);
                int endRow = Integer.parseInt(moveParts[2]);
                int endCol = Integer.parseInt(moveParts[3]);

                if (isValidMove(startRow, startCol, endRow, endCol)) {
                    System.out.println("Move is valid.");
                } else {
                    System.out.println("Move is invalid.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid move format. Please try again.");
            }
        }

        scanner.close();
    }

    private static char[][] parseFEN(String fen) {
        char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
        String[] rows = fen.split("/");
        for (int i = 0; i < BOARD_SIZE; i++) {
            int colIndex = 0;
            for (char c : rows[i].toCharArray()) {
                if (Character.isDigit(c)) {
                    int emptySquares = Character.getNumericValue(c);
                    for (int j = 0; j < emptySquares; j++) {
                        board[i][colIndex++] = ' ';
                    }
                } else {
                    board[i][colIndex++] = c;
                }
            }
        }
        return board;
    }

    private static boolean isValidMove(int startRow, int startCol, int endRow, int endCol) {
        if (startRow < 0 || startRow >= BOARD_SIZE || startCol < 0 || startCol >= BOARD_SIZE ||
            endRow < 0 || endRow >= BOARD_SIZE || endCol < 0 || endCol >= BOARD_SIZE) {
            return false;
        }

        char piece = boardState[startRow][startCol];
        if (piece == ' ') return false;

        List<int[]> validMoves = getValidMoves(startRow, startCol, piece);
        for (int[] move : validMoves) {
            if (move[0] == endRow && move[1] == endCol) {
                return true;
            }
        }
        return false;
    }

    private static List<int[]> getValidMoves(int row, int col, char piece) {
        List<int[]> moves = new ArrayList<>();
        switch (Character.toLowerCase(piece)) {
            case 'p':
                int direction = Character.isLowerCase(piece) ? 1 : -1;
                if (row + direction >= 0 && row + direction < BOARD_SIZE && boardState[row + direction][col] == ' ') {
                    moves.add(new int[]{row + direction, col});
                    if ((row == 1 && Character.isLowerCase(piece)) || (row == 6 && Character.isUpperCase(piece))) {
                        if (!boardState[row + 2 * direction][col]) {
                            moves.add(new int[]{row + 2 * direction, col});
                        }
                    }
                }
                if (row + direction >= 0 && row + direction < BOARD_SIZE) {
                    if (col - 1 >= 0 && boardState[row + direction][col - 1] != ' ' && boardState[row + direction][col - 1] != piece) {
                        moves.add(new int[]{row + direction, col - 1});
                    }
                    if (col + 1 < BOARD_SIZE && boardState[row + direction][col + 1] != ' ' && boardState[row + direction][col + 1] != piece) {
                        moves.add(new int[]{row + direction, col + 1});
                    }
                }
                break;
            case 'r':
                for (int i = row + 1; i < BOARD_SIZE; i++) {
                    if (boardState[i][col] != ' ') {
                        if (boardState[i][col] != piece) moves.add(new int[]{i, col});
                        break;
                    }
                    moves.add(new int[]{i, col});
                }
                for (int i = row - 1; i >= 0; i--) {
                    if (boardState[i][col] != ' ') {
                        if (boardState[i][col] != piece) moves.add(new int[]{i, col});
                        break;
                    }
                    moves.add(new int[]{i, col});
                }
                for (int i = col + 1; i < BOARD_SIZE; i++) {
                    if (boardState[row][i] != ' ') {
                        if (boardState[row][i] != piece) moves.add(new int[]{row, i});
                        break;
                    }
                    moves.add(new int[]{row, i});
                }
                for (int i = col - 1; i >= 0; i--) {
                    if (boardState[row][i] != ' ') {
                        if (boardState[row][i] != piece) moves.add(new int[]{row, i});
                        break;
                    }
                    moves.add(new int[]{row, i});
                }
                break;
            case 'n':
                int[][] knightMoves = {
                    {row - 2, col - 1}, {row - 2, col + 1},
                    {row - 1, col - 2}, {row - 1, col + 2},
                    {row + 1, col - 2}, {row + 1, col + 2},
                    {row + 2, col - 1}, {row + 2, col + 1}
                };
                for (int[] move : knightMoves) {
                    if (move[0] >= 0 && move[0] < BOARD_SIZE && move[1] >= 0 && move[1] < BOARD_SIZE) {
                        if (boardState[move[0]][move[1]] == ' ' || boardState[move[0]][move[1]] != piece) {
                            moves.add(move);
                        }
                    }
                }
                break;
            case 'b':
                for (int i = 1; row + i < BOARD_SIZE && col + i < BOARD_SIZE; i++) {
                    if (boardState[row + i][col + i] != ' ') {
                        if (boardState[row + i][col + i] != piece) moves.add(new int[]{row + i, col + i});
                        break;
                    }
                    moves.add(new int[]{row + i, col + i});
                }
                for (int i = 1; row - i >= 0 && col - i >= 0; i++) {
                    if (boardState[row - i][col - i] != ' ') {
                        if (boardState[row - i][col - i] != piece) moves.add(new int[]{row - i, col - i});
                        break;
                    }
                    moves.add(new int[]{row - i, col - i});
                }
                for (int i = 1; row + i < BOARD_SIZE && col - i >= 0; i++) {
                    if (boardState[row + i][col - i] != ' ') {
                        if (boardState[row + i][col - i] != piece) moves.add(new int[]{row + i, col - i});
                        break;
                    }
                    moves.add(new int[]{row + i, col - i});
                }
                for (int i = 1; row - i >= 0 && col + i < BOARD_SIZE; i++) {
                    if (boardState[row - i][col + i] != ' ') {
                        if (boardState[row - i][col + i] != piece) moves.add(new int[]{row - i, col + i});
                        break;
                    }
                    moves.add(new int[]{row - i, col + i});
                }
                break;
            case 'q':
                moves.addAll(getValidMoves(row, col, Character.isLowerCase(piece) ? 'r' : 'R'));
                moves.addAll(getValidMoves(row, col, Character.isLowerCase(piece) ? 'b' : 'B'));
                break;
            case 'k':
                int[][] kingMoves = {
                    {row - 1, col - 1}, {row - 1, col}, {row - 1, col + 1},
                    {row, col - 1}, {row, col + 1},
                    {row + 1, col - 1}, {row + 1, col}, {row + 1, col + 1}
                };
                for (int[] move : kingMoves) {
                    if (move[0] >= 0 && move[0] < BOARD_SIZE && move[1] >= 0 && move[1] < BOARD_SIZE) {
                        if (boardState[move[0]][move[1]] == ' ' || boardState[move[0]][move[1]] != piece) {
                            moves.add(move);
                        }
                    }
                }
                break;
        }
        return moves;
    }
}