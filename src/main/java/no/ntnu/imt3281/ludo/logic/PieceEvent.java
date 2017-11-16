package no.ntnu.imt3281.ludo.logic;

import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A piece event
 * 
 * @author Marius
 *
 */
@SuppressWarnings("serial")
public class PieceEvent extends EventObject implements PieceListener {
    private static final Logger LOGGER = Logger.getLogger(PieceEvent.class.getName());
    private int player;
    private int piece;
    private int from;
    private int to;

    /**
     * A constructor
     * 
     * @param ludo
     *            The gameboard to construct this event with
     */
    public PieceEvent(Ludo ludo) {
        super(ludo);
    }

    /**
     * A constructor
     * 
     * @param ludo
     *            The gameboard to construct this event with
     * @param player
     *            The player index
     * @param piece
     *            The piece index
     * @param from
     *            Tile moved from
     * @param to
     *            Tile moved to
     */
    public PieceEvent(Ludo ludo, int player, int piece, int from, int to) {
        super(ludo);
        this.player = player;
        this.piece = piece;
        this.from = from;
        this.to = to;
    }

    @Override
    public void pieceMoved(PieceEvent event) {
        player = event.player;
        piece = event.piece;
        from = event.piece;
        to = event.to;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other == null || this.getClass() != other.getClass())) {
            try {
                PieceEvent event = (PieceEvent) other;
                return player == event.player && piece == event.piece && from == event.from
                        && to == event.to;
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (player * piece - from / 5 + to) * 17 % 1025;
    }

    @Override
    public String toString() {
        return player + "," + piece + "," + from + "," + to;
    }
}
