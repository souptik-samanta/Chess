import java.util.*;

public class ChessValidator {
    private char[][] board;
    private boolean whiteToMove;
    private boolean[] castlingRights;
    private int[] enPassantSquare;
    private int halfMoveClock;
    private int fullMoveNumber;

    public ChessValidator(String fen) {
        board = new char[8][8];
        castlingRights = new boolean[4];
        parseFEN(fen);
    }

    private void parseFEN(String fen) {
        String[] parts = fen.split(" ");
        String position = parts[0];
        
        int rank = 0;
        int file = 0;
        for (char c : position.toCharArray()) {
            if (c == '/') {
                rank++;
                file = 0;
            } else if (Character.isDigit(c)) {
                file += Character.getNumericValue(c);
            } else {
                board[rank][file] = c;
                file++;
            }
        }

        whiteToMove = parts[1].equals("w");

        if (!parts[2].equals("-")) {
            for (char c : parts[2].toCharArray()) {
                switch (c) {
                    case 'K': castlingRights[0] = true; break;
                    case 'Q': castlingRights[1] = true; break;
                    case 'k': castlingRights[2] = true; break;
                    case 'q': castlingRights[3] = true; break;
                }
            }
        }

        if (!parts[3].equals("-")) {
            enPassantSquare = new int[2];
            enPassantSquare[1] = parts[3].charAt(0) - 'a';
            enPassantSquare[0] = '8' - parts[3].charAt(1);
        }

        halfMoveClock = Integer.parseInt(parts[4]);
        fullMoveNumber = Integer.parseInt(parts[5]);
    }

    public void makeMove(String move) {
        int startFile = move.charAt(0) - 'a';
        int startRank = '8' - move.charAt(1);
        int endFile = move.charAt(2) - 'a';
        int endRank = '8' - move.charAt(3);

        char piece = board[startRank][startFile];
        char capturedPiece = board[endRank][endFile];

        if (Character.toLowerCase(piece) == 'p' || capturedPiece != 0) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }

        // Handle en passant capture
        if (Character.toLowerCase(piece) == 'p' && 
            enPassantSquare != null &&
            endFile == enPassantSquare[1] && 
            endRank == enPassantSquare[0]) {
            board[startRank][endFile] = 0;
        }

        // Handle castling
        if (Character.toLowerCase(piece) == 'k' && Math.abs(endFile - startFile) == 2) {
            int rookStartFile = endFile > startFile ? 7 : 0;
            int rookEndFile = endFile > startFile ? endFile - 1 : endFile + 1;
            char rook = board[startRank][rookStartFile];
            board[startRank][rookStartFile] = 0;
            board[startRank][rookEndFile] = rook;
        }

        updateCastlingRights(startRank, startFile, endRank, endFile);

        board[endRank][endFile] = piece;
        board[startRank][startFile] = 0;

        if (Character.toLowerCase(piece) == 'p' && Math.abs(startRank - endRank) == 2) {
            enPassantSquare = new int[]{(startRank + endRank) / 2, startFile};
        } else {
            enPassantSquare = null;
        }

        if (!whiteToMove) {
            fullMoveNumber++;
        }
        whiteToMove = !whiteToMove;
    }

    private void updateCastlingRights(int startRank, int startFile, int endRank, int endFile) {
        if (board[startRank][startFile] == 'K') {
            castlingRights[0] = false;
            castlingRights[1] = false;
        } else if (board[startRank][startFile] == 'k') {
            castlingRights[2] = false;
            castlingRights[3] = false;
        }

        if (startRank == 0 && startFile == 0) castlingRights[3] = false;
        if (startRank == 0 && startFile == 7) castlingRights[2] = false;
        if (startRank == 7 && startFile == 0) castlingRights[1] = false;
        if (startRank == 7 && startFile == 7) castlingRights[0] = false;

        if (endRank == 0 && endFile == 0) castlingRights[3] = false;
        if (endRank == 0 && endFile == 7) castlingRights[2] = false;
        if (endRank == 7 && endFile == 0) castlingRights[1] = false;
        if (endRank == 7 && endFile == 7) castlingRights[0] = false;
    }

    public String getFEN() {
        StringBuilder fen = new StringBuilder();
        
        for (int rank = 0; rank < 8; rank++) {
            int emptyCount = 0;
            for (int file = 0; file < 8; file++) {
                if (board[rank][file] == 0) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(board[rank][file]);
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (rank < 7) {
                fen.append('/');
            }
        }

        fen.append(' ').append(whiteToMove ? 'w' : 'b');

        fen.append(' ');
        boolean hasCastling = false;
        if (castlingRights[0]) { fen.append('K'); hasCastling = true; }
        if (castlingRights[1]) { fen.append('Q'); hasCastling = true; }
        if (castlingRights[2]) { fen.append('k'); hasCastling = true; }
        if (castlingRights[3]) { fen.append('q'); hasCastling = true; }
        if (!hasCastling) { fen.append('-'); }

        fen.append(' ');
        if (enPassantSquare != null) {
            fen.append((char)('a' + enPassantSquare[1]));
            fen.append((char)('8' - enPassantSquare[0]));
        } else {
            fen.append('-');
        }

        fen.append(' ').append(halfMoveClock);
        fen.append(' ').append(fullMoveNumber);

        return fen.toString();
    }

    public void printBoard() {
        System.out.println("  a b c d e f g h");
        for (int rank = 0; rank < 8; rank++) {
            System.out.print((8 - rank) + " ");
            for (int file = 0; file < 8; file++) {
                char piece = board[rank][file];
                System.out.print((piece == 0 ? '.' : piece) + " ");
            }
            System.out.println(8 - rank);
        }
        System.out.println("  a b c d e f g h");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ChessValidator chess = null;

        while (true) {
            if (chess == null) {
                System.out.println("\nEnter FEN position (or 'exit' to quit):");
                String fen = scanner.nextLine();
                
                if (fen.equalsIgnoreCase("exit")) {
                    break;
                }

                try {
                    chess = new ChessValidator(fen);
                    System.out.println("\nCurrent position:");
                    chess.printBoard();
                } catch (Exception e) {
                    System.out.println("Invalid FEN! Please try again.");
                    continue;
                }
            }

            System.out.println("\nEnter move (e.g., e2e4) or:");
            System.out.println("- 'fen' to input new position");
            System.out.println("- 'board' to show current position");
            System.out.println("- 'exit' to quit");
            
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                break;
            } else if (input.equalsIgnoreCase("fen")) {
                chess = null;
                continue;
            } else if (input.equalsIgnoreCase("board")) {
                chess.printBoard();
                System.out.println("Current FEN: " + chess.getFEN());
                continue;
            }

            try {
                if (input.length() == 4) {
                    chess.makeMove(input);
                    System.out.println("Made move! New position:");
                    chess.printBoard();
                    System.out.println("New FEN: " + chess.getFEN());
                } else {
                    System.out.println("Invalid move format! Use format like 'e2e4'");
                }
            } catch (Exception e) {
                System.out.println("Error making move! Try again.");
            }
        }
        
        System.out.println("Goodbye!");
        scanner.close();
    }
}