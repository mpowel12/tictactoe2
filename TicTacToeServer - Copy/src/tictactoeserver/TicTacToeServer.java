/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tictactoeserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Mitchell
 */
public class TicTacToeServer {

    //pairs up 2 users and allows them to start a game
    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(8901);
        System.out.println("Tic Tac Toe Server is Running");
        try {
            while (true) {
                TicTacToe game = new TicTacToe();
                TicTacToe.User userX = game.new User(listener.accept(), 'X');
                TicTacToe.User userO = game.new User(listener.accept(), 'O');
                userX.setOpponent(userO);
                userO.setOpponent(userX);
                game.currentUser = userX;
                userX.start();
                userO.start();
            }
        } finally {
            listener.close();
        }
    }
}

class TicTacToe {
    User currentUser;
    private User[] board = {
        null, null, null, 
        null, null, null, 
        null, null, null};

    //win conditions
    public boolean hasWinner() {
        return
            //veritcal win conditions
            (board[0] == board[3] && board[0] == board[6]&&board[0]!= null&&board[3]!=null && board[6]!=null)||(board[1] == board[4] && board[1] == board[7]&&board[1]!= null&&board[4]!=null && board[7]!=null)||(board[2] == board[5] && board[2] == board[8]&&board[2]!= null&&board[5]!=null && board[8]!=null)||
             //horizontal win conditions
            (board[0] == board[1] && board[0] == board[2]&&board[0]!= null&&board[1]!=null && board[2]!=null)||(board[3] == board[4] && board[3] == board[5]&&board[3]!= null&&board[4]!=null && board[5]!=null)||(board[6] == board[7] && board[6] == board[8]&&board[6]!= null&&board[7]!=null && board[8]!=null)||
            //diagonal win conditions
            (board[0] == board[4] && board[0] == board[8]&&board[0]!= null&&board[4]!=null && board[8]!=null)||(board[2] == board[4] && board[2] == board[6]&&board[2]!= null&&board[4]!=null && board[6]!=null);
    }

    public boolean boardFull() {
        int i = 0;
        while(i<board.length){
            if (board[i] == null) return false;
            i++;
            
        }
        return true;
    }

    
    public synchronized boolean validMove(int location, User user) {
        if (user == currentUser && board[location] == null) {
            board[location] = currentUser;
            currentUser = currentUser.opponent;
            currentUser.theyMoved(location);
            return true;
        }
        return false;
    }

   
    class User extends Thread {
        char mark;
        User opponent;
        Socket socket;
        BufferedReader input;
        PrintWriter output;

       
        public User(Socket socket, char mark) {
            this.socket = socket;
            this.mark = mark;
            try {
                input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("Connected " + mark);
                output.println("Message Waiting for opponent to connect");
            } catch (IOException e) {
                System.out.println("User died: " + e);
            }
        }

        
        public void setOpponent(User opponent) {
            this.opponent = opponent;
        }

        
        public void theyMoved(int location) {
            output.println("They_Moved " + location);
            
                    if(hasWinner()){
                        output.println("Loss");
                    }
                    if(boardFull()){
                        output.println("Tie");
                        
                    }
                    else output.println("");
                
        }
        public void run() {
            try {   
                output.println("Message All users connected");              
                if (mark == 'X') {
                    output.println("Message Your move");
                }

                while (true) {
                    String command = input.readLine();
                    if (command.startsWith("Move")) {
                        int location = Integer.parseInt(command.substring(5));
                        if (validMove(location, this)) {
                            output.println("valid_move");
                            if(hasWinner())output.println("Win");
                            else if(boardFull())output.println("Tie");
                            else output.println("");
                            
                        } else {
                            output.println("Message Other Player's Turn");
                        }
                    } else if (command.startsWith("Stop")) {
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.println("User died: " + e);
            } finally {
                try {socket.close();} catch (IOException e) {}
            }
        }
    }
}