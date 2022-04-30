package it.unibo.ai.didattica.competition.tablut.gionnino9000.heuristics;

import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.HashMap;
import java.util.Map;

public class BlackHeuristics extends Heuristics{

    private final int WHITE_EATEN = 0;
    private final int BLACK_ALIVE = 1;
    private final int BLACK_SUR_K = 2;
    private final int RHOMBUS_POS = 3;

    // Flag to enable console print
    private boolean print = false;

    // Numb. tiles in rhombus
    private final int TILES_IN_RHOMBUS = 8;

    // weights for evaluation in the following order: WhiteEaten, BlackAlive, BlackSurroundingKing, RhombusPos
    private final Double[] earlyGameWeights;
    private final Double[] lateGameWeights;

    // Matrix of favourite black positions in the initial stages to block the escape ways
    private final int[][] rhombus = {
                  {1,2},       {1,6},
            {2,1},                   {2,7},

            {6,1},                   {6,7},
                  {7,2},       {7,6}
    };

    public BlackHeuristics(State state) {
        super(state);

        earlyGameWeights = new Double[4];
        earlyGameWeights[WHITE_EATEN] = 50.0;
        earlyGameWeights[BLACK_ALIVE] = 35.0;
        earlyGameWeights[BLACK_SUR_K] = 10.0;
        earlyGameWeights[RHOMBUS_POS] = 5.0;

        lateGameWeights = new Double[4];
        lateGameWeights[WHITE_EATEN] = 40.0;
        lateGameWeights[BLACK_ALIVE] = 30.0;
        lateGameWeights[BLACK_SUR_K] = 25.0;
        lateGameWeights[RHOMBUS_POS] = 5.0;

    }

    /**
     * @return the evaluation of the states using a weighted sum
     */
    @Override
    public double evaluateState() {
        double stateValue = 0.0;
        boolean lateGame = false;

        int numbOfWhite = state.getNumberOf(State.Pawn.WHITE);
        if (numbOfWhite < 4)
            lateGame = true;

        // Values for the weighted sum
        double numberOfBlackAlive = (double) state.getNumberOf(State.Pawn.BLACK) / GameAshtonTablut.NUM_BLACK;
        double numberOfWhiteEaten = (double) (GameAshtonTablut.NUM_WHITE - numbOfWhite) / GameAshtonTablut.NUM_WHITE;
        double pawnsNearKing = (double) checkAdjacentPawns(state, kingPosition(state), State.Turn.BLACK.toString()) / getNumbToEatKing(state);
        double pawnsInRhombus = (double) getRhombusValue() / TILES_IN_RHOMBUS;

        if (print) {
            System.out.println("Black pawns alive: " + numberOfBlackAlive);
            System.out.println("Number on rhombus pos: " + pawnsInRhombus);
            System.out.println("Number of pawns near to the king:" + pawnsNearKing);
            System.out.println("Number of white pawns eaten: " + numberOfWhiteEaten);
        }

        if (!lateGame) {
            stateValue += numberOfWhiteEaten * earlyGameWeights[WHITE_EATEN];
            stateValue += numberOfBlackAlive * earlyGameWeights[BLACK_ALIVE];
            stateValue += pawnsNearKing * earlyGameWeights[BLACK_SUR_K];
            stateValue += pawnsInRhombus * earlyGameWeights[RHOMBUS_POS];

            if (print) {
                System.out.println("|EARLY_GAME|: value is " + stateValue);
            }
        } else {
            stateValue += numberOfWhiteEaten * lateGameWeights[WHITE_EATEN];
            stateValue += numberOfBlackAlive * lateGameWeights[BLACK_ALIVE];
            stateValue += pawnsNearKing * lateGameWeights[BLACK_SUR_K];
            stateValue += pawnsInRhombus * lateGameWeights[RHOMBUS_POS];

            if (print) {
                System.out.println("|LATE_GAME|: value is " + stateValue);
            }
        }

        return stateValue;
    }

    /**
     * @return number of black pawns on tiles if we have enough pawns
     */
    public int getRhombusValue(){
        if (state.getNumberOf(State.Pawn.BLACK) >= 10) {
            return pawnsInRhombus();
        }else{
            return 0;
        }
    }

    /**
     * @return number of black pawns on rhombus configuration
     */
    public int pawnsInRhombus(){
        int count = 0;

        for (int[] position : rhombus) {
            if (state.getPawn(position[0], position[1]).equalsPawn(State.Pawn.BLACK.toString())) {
                count++;
            }
        }
        return count;
    }
}
