package kgorlen.games.connect4;

//import kgorlen.games;

public class Connect4Position {
    static final long colMask = 0x3f3f3f3f3f3f3fL; 	// Mask for column bits
    static final long firstRowMask = 0x01010101010101L; // Mask for bits in first row
    static final long[] rowMasks = { // Masks for bits in Ith row
	0x01010101010101L,
	0x02020202020202L,
	0x04040404040404L,
	0x08080808080808L,
	0x10101010101010L,
	0x20202020202020L
    };
    static final long[] colMasks = { // Masks for bits in Ith col
	0x0000000000003fL,
	0x00000000003f00L,
	0x000000003f0000L,
	0x0000003f000000L,
	0x00003f00000000L,
	0x003f0000000000L,
	0x3f000000000000L
    };
    static final int[] colShift = { 0, 8, 16, 24, 32, 40, 48 };

    private long[] board;	// Mask for cells occupied by black (board[0]) and red (board[1])
    private int moveNum;	// Number of moves; black moves first
    
/*
 * Utility methods.
 * 
 * "final" is hint to javac to expand calls to these simple member functions inline.
 */
	
	public final int numMoves() { 		// Return number moves made
		return moveNum;
    }

	public final String sideToMove() { 	// Return 0 = X, 1 = O
		return ((moveNum & 1) == 0) ? "X" : "O";
    }

	public final int colorToMove() { 	// Return 1 = X, -1 = O
		return ((moveNum & 1) == 0) ? 1 : -1;
    }
	
	public final int numEmpty()	{	// Return number of empty squares
		return 9 - moveNum;
	}
	
	public final long occupied() {	// Return mask of occupied squares
		return (board[0] | board[1]);
    }
    
	public final long empty()	{	// Return mask of empty squares
		return (~(board[0] | board[1]) & colMask);
    }
	
    public long moves() {	// Return mask of legal moves
    	return ((board[0] | board[1]) + firstRowMask) & colMask;
    }
    
    public void print() {	// Print board
		for (int i = 5; i >= 0; i--) {
		    String row = "";
		    for (int j = 0; j < 7; j++) {
				if ((board[0] & rowMasks[i] & colMasks[j]) != 0) {
				    row += 'X';
				} else if ((board[1] & rowMasks[i] & colMasks[j]) != 0) {
				    row += 'O';
				} else {
				    row += '.';
				}
			}
		    System.out.println(row);
	    }
	}


	/**
	 * Check specified move for validity.
	 * 
	 * @param mv	bitmask of move
	 * @return		true if move is valid; otherwise false
	 */
	public boolean isValidMove(long mv) {
		if ((mv & colMask) == 0 )		// Invalid move mask
			return false;
		
		if ((mv & ~colMask) != 0 )	// Invalid move mask
			return false;
		
		if ((occupied() & mv) != 0)	// Square already occupied
			return false;
		
		return true;
    }	

	/**
	 * Make specified move.
	 * 
	 * @param mv	bitmask of move
	 */
	public void makeMove(long mv) {

		assert isValidMove(mv): "Invalid move: 0x" + Long.toHexString(mv);
		
		board[moveNum & 1] |= mv;
		moveNum++;
		return;
    }

	/**
     * @return	true if last color moved made 4-in-a-row
     */
    static final int[] dirShift = { 1, 7, 8, 9 };

    public boolean isWin() {
		if (moveNum < 7) return false;	// Win requires at least 7 moves
   	
		long m;			// mask for cells occupied by color last moved
		for (int direction = 0; direction < dirShift.length; direction++) {
		    m = board[~moveNum & 1];
		    m &= m << dirShift[direction];
		    if (m == 0) continue;
		    m &= m << dirShift[direction];
		    if (m == 0) continue;
		    m &= m << dirShift[direction];
		    if (m != 0) return true;
		}
		return false;
    }

}
