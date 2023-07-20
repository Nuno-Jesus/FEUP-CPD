# Blacjack

## Table of Contents

- [Description](#description)
- [Game Modes](#game-modes)
  - [Simple Mode (Non-Ranked)](#simple-mode-non-ranked)
  - [Ranked Mode](#ranked-mode)
- [How to Play / Setup Client-Server connection](#how-to-play--setup-client-server-connection)
- [Disconnecting](#disconnecting)
- [Concurrency](#concurrency)

## Description

BlackJack is a card game where the goal is to get as close to 21 as possible 
without going over. The game is played with a standard 52 card deck.
Even though the original player is played against a dealer, our version consists of 4 players, with the dealer
not being one of them. 
The game is played in rounds:

- In the first round, every player is dealt 1 card.
- In the following rounds, players take turns and can either ask for another card or stay (cannot ask for more cards).

If a player goes over 21, they are out of the game.

The winner(s) is/are the player(s) with the highest score that did not go over 21.

## Game Modes

There are two game modes: simple and ranked. While both contribute to a player's overall elo, only the ranked
mode considers the elo of the other players when creating a game.

The elo is calculated as follows:
- The winners get +10 elo points.
- The losers get -5 elo points.

### Simple Mode (Non-Ranked)

In the simple mode, a game starts automatically when 4 players join the queue. 

### Ranked Mode

In the ranked mode, 4 players in the queue are not sufficient to start a game. 
Instead, the game will wait so that 4 players with similar elo can be matched together.

To ensure users don't have to wait too long, the maximum elo difference allowed is relaxed as time passes.
It starts at 5 and increases by 5 every 10 seconds.

## How to Play / Setup Client-Server connection

Assuming you're running the project on IntelliJ, please follow the steps below:

1. Edit GameServer configuration and add `8080` as a CLI argument.
   1. Go to [GameServerMain](../src/main/java/org/example/GameServerMain.java)
   2. Right click on the ▶️ button next to the `main` method
   3. Select `Modify Run Configuration`
   4. Add 8080 as a program argument
   5. Click `Apply` and `OK`
2. For the GameClient, do the same and allow for multiple instances (clients) to be run:
    1. Go to [GameClientMain](../src/main/java/org/example/GameClientMain.java)
    2. Right-click on the ▶️ button next to the `main` method
    3. Select `Modify Run Configuration`
    4. Add 8080 as a program argument
    5. Click on `Modify Options` (or ALT+M) and select `Allow multiple instances` under the `Operating System` tab
    6. Click `Apply` and `OK`

3. Run the GameServer, wait for it to print `Server is listening on port 8080` and then run as many GameClients as you want
(4 are needed to start a game)

4. On the game client, insert a username (> 5 characters) and a password and press 'ENTER' to login. It'll let you know if
the username is too short of if the password is incorrect.
5. After that, select the game mode you want to play (simple or ranked) and wait for the game to start.
6. During the game, when your turn arrives, you can either ask for another card or stay. The server will let you know your
current score. If you go over 21, you'll be out of the game.
7. When the game ends, you'll be able to see who won the game.
8. After that, the client is close gracefully and you can run another one to play again.

## Disconnecting

If you were in a queue for a game but you disconnect, you have 20 seconds to reconnect. 
If you don't, you'll be removed from the queue. Otherwise, you'll rejoin at the same position you were before.

## Concurrency

Our server starts 2 parallel threads on startup, with tasks we will explain below.
Each authentication request is handled by a new thread.

The server is able to handle multiple clients at the same time. This is because whenever the server receives a request to authenticate, it creates a new thread to handle that request.

If the incoming request can complete a batch to start a game, that thread will not only authenticate the user, but also take care of the game execution (invoking the rest of the awaiting players and start the game). Therefore, multiple instances of a game can be running at the same time.

Because we need to check if a player has disconnected or not, we use one of the 2 mentioned threads to analyse the queue and iterate over each player, to test if they're still connected or not (this is done by checking if the player is sending an ACK string to the server, if not, it means they've disconnected). This is where concurrency comes into play, as we need to make sure that the queue is not being modified while we're iterating over it. To do so, we use a `ReentrantLock` to lock the queue when authentication occurs and also when checking for disconnections.

Whenever a game finishes, the server has the task of updating the database, which is nothing more than a JSON file. However, multiple games cannot update the file at the same time, since we have another `ReentrantLock` to lock the file when it's being updated.

Finally, we setted up another thread to analyse the ranked queue solely. If the thread detects a large waiting time frame, it increases the maximum elo difference allowed between players. Here, we also look through a queue, so we also need to lock it (using the first mentioned lock).
