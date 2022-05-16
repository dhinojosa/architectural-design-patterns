package com.jitterted.ebp.blackjack;

import com.jitterted.ebp.blackjack.adapter.in.console.ConsoleGame;
import com.jitterted.ebp.blackjack.domain.Deck;
import com.jitterted.ebp.blackjack.domain.Game;

// Startup-Assembler class
public class Blackjack {

    // Creates, Assembles (wires), and Configures objects together
    // This class is Transient
    public static void main(String[] args) {
        Game game = new Game(new Deck()); // Entity-like object
        ConsoleGame consoleGame = new ConsoleGame(game); // in general: Entities aren't directly passed in to Adapters
        consoleGame.start();
    }

}
