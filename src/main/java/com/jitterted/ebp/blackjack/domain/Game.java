package com.jitterted.ebp.blackjack.domain;

public class Game {

    private final Deck deck;

    private final Hand dealerHand = new Hand();
    private final Hand playerHand = new Hand();

    private boolean playerDone;

    public Game(Deck deck) {
        this.deck = deck;
    }

    public void initialDeal() {
        dealRoundOfCards();
        dealRoundOfCards();
        if (playerHand.hasBlackjack()) {
            playerDone = true;
        }
    }

    private void dealRoundOfCards() {
        // why: players first because this is the rule
        playerHand.drawFrom(deck);
        dealerHand.drawFrom(deck);
    }

    public GameOutcome determineOutcome() {
        if (playerHand.isBusted()) {
            return GameOutcome.PLAYER_BUSTED;
        } else if (dealerHand.isBusted()) {
            return GameOutcome.DEALER_BUSTED;
        } else if (playerHand.hasBlackjack()) {
            return GameOutcome.PLAYER_WINS_BLACKJACK;
        } else if (playerHand.beats(dealerHand)) {
            return GameOutcome.PLAYER_BEATS_DEALER;
        } else if (playerHand.pushes(dealerHand)) {
            return GameOutcome.PLAYER_PUSHES;
        } else {
            return GameOutcome.PLAYER_LOSES;
        }
    }

    // IDEMPOTENT
    private void dealerTurn() {
        // Dealer makes its choice automatically based on a simple heuristic (<=16 must hit, =>17 must stand)
        if (!playerHand.isBusted()) {
            while (dealerHand.dealerMustDrawCard()) {
                dealerHand.drawFrom(deck);
            }
        }
    }

    // QUERY METHOD RULE
    // -> SNAPSHOT (point-in-time)
    // -> DON'T REVEAL internals: PREVENT illegal change to Game's state
    // 0. return Hand -> it's mutable, outside control of Game; it's not Snapshot => NO
    // 1. Copy (clone) Hand -> is snapshot; not reveal internals => Misleading => maybe
    //  1a. rename to copyOfPlayerHand()
    // 2. Interface "ReadOnlyHand", that Hand implements: cards(), value(), dealerFaceUpCard()
    //              --> prevents change to Game's state, not a Snapshot
    // 3. DTO ("HandDto") - cards, value, dealerFaceUpCard => Adapter-specific classes, JavaBean naming conventions
    // 4. Value Object ("HandView") -> cards(), value(), dealerFaceUpCard() => YES (maybe a Record)
    //      -> copying would happen here or via hand.asView() method
    public Hand playerHand() {
        return playerHand;
    }

    public Hand dealerHand() {
        return dealerHand;
    }


    public void playerHits() {
        playerHand.drawFrom(deck);
        playerDone = playerHand.isBusted();
    }

    public void playerStands() {
        playerDone = true;
        dealerTurn();
    }

    public boolean isPlayerDone() {
        return playerDone;
    }

}
