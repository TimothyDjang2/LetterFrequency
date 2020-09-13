/**
 * Letter Frequency Counter - Made by Timothy Djang for CS-172, Sep 11, 2020
 * 
 * Features:
 * - No external libraries because i'm too lazy to make them work in my IDE.
 * - Exception handling for when you inevitably type the file name wrong.
 * - Too many comments.
 */


// It's more effort for me to use the stdlib library since I can't figure out how to make intellisense work for external jars.
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

// Ok it might actually still be less effort to use the library but I really like having intellisense.
import javax.swing.JFrame;

// Without intellisense working correctly VSCode is gonna say my code is full of syntax errors and that i wrote bad code, and i crave validation more than efficiency
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.Font;

public class LetterCounter {

    //This is the line you change to switch the file name. Searches the directory you run this program in.
    public static final String filepath = "./2701-0.txt";

    public static final int WINDOW_WIDTH = 800, WINDOW_HEIGHT = 640, BAR_SIZE = 22; //Constants for what the histogram window should look like.
    public static final int FONT_SIZE = BAR_SIZE - 2; // Exists for code readability only, do not change this value.

    public static void main(String[] args) {

        int[] chars = readFile(); // Decided to put file reading in a seperate method to make main() less big, and to keep any FileIO exception handling stuff in a separate spot.

        if (chars == null) return; // chars will be null if readFile() couldn't find whatever filepath we gave it.
        
        // Since the array we get from readFile() is sorted alphabetically i can just iterate down it instead of having to have a key that tells me what letter each frequency corresponds to
        for (int i = 0; i < 26; i++) {
            char letter = (char)('a' + i);
            System.out.println(letter + ": " + chars[i]);
        }

        // Get the amount of times the letter with the highest frequency was used. This will be used to set scaling for the histogram.
        double maxChars = 0;
        for (int i : chars) {
            if (i > maxChars) maxChars = i;
        }

        // The library would make all of this much easier but again, intellisense no work for external jars, so any code i write with it would just be considered a giant block of errors.
        JFrame window = new JFrame("LetterCounter");
        Canvas renderpane = new Canvas();
        Graphics graphics = null;
        BufferStrategy buffer;

        window.add(renderpane);
        window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
        window.setIgnoreRepaint(true); // Theoretically this line here should allow me to not use the while loop down there but it doesn't so that's disappointing.
        window.setResizable(false);

        renderpane.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        renderpane.setIgnoreRepaint(true);

        renderpane.createBufferStrategy(2); // Use two buffers to draw to the window so it doesn't flicker if your computer is slow.
        buffer = renderpane.getBufferStrategy();

        // If i don't put this in a while loop the OS just decides to repaint the canvas blank. I could override the paint() method to fix this but then using the array as a source of data would be much harder.
        // I know this is really sketch since the while loop doesn't have an exit condition, but closing the window exits the program so it's ok.
        while (true) {
            graphics = buffer.getDrawGraphics(); // Getting the graphics component associated with the buffer allows you to draw stuff onto the buffer.
            
            graphics.clearRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

            // Draw the axes and scale numbers
            graphics.setFont(new Font("Arial", Font.PLAIN, FONT_SIZE));
            graphics.setColor(Color.BLACK);
            graphics.fillRect(100, 60, 3, WINDOW_HEIGHT - 180);
            graphics.fillRect(100, WINDOW_HEIGHT - 123, WINDOW_WIDTH - 200, 3);
            graphics.fillRect(95, 60, 13, 3);
            graphics.drawString((int)maxChars + "", 80 - ((int)(FONT_SIZE / 2.5) * String.valueOf(maxChars).length()), 60 + ((2 * FONT_SIZE) / 3));
            graphics.drawString("0", 80 - (FONT_SIZE / 3) , WINDOW_HEIGHT - 120);
            
            // Draw all the letters along the bottom.
            for (int i = 0; i < 26; i++) {
                graphics.drawString((char)('a' + i) + "", 110 + (BAR_SIZE * i), WINDOW_HEIGHT - 115 + ((2 * FONT_SIZE) / 3));
            }

            for (int i = 0; i < 26; i++) {
                graphics.setColor(Color.RED);
                graphics.fillRect(105 + (BAR_SIZE * i), ((WINDOW_HEIGHT - 123) - (int)(((double)chars[i] / maxChars) * (WINDOW_HEIGHT - 183))), BAR_SIZE - 4, (int)(((double)chars[i] / maxChars) * (WINDOW_HEIGHT - 183)));
                graphics.setColor(Color.BLACK);
                graphics.drawRect(105 + (BAR_SIZE * i), ((WINDOW_HEIGHT - 123) - (int)(((double)chars[i] / maxChars) * (WINDOW_HEIGHT - 183))), BAR_SIZE - 4, (int)(((double)chars[i] / maxChars) * (WINDOW_HEIGHT - 183)));
                // Uh oh big complicated math what does it mean.
                // This thing is the important bit, used for bar height. vvv
                // (int)(((double)chars[i] / maxChars) * (WINDOW_HEIGHT - 183))
                // It calculates the percentage of the maximum character count for this letter (how much is it used relative to the most-used letter) and then multiplys that by
                // the total allowed height for a bar, in order to figure out how tall the bar for this letter should be.
                // The disgusting typecasting stuff in the middle is to make sure it actually calculates a percentage, instead of just giving me 1 or 0.
                // Basically everything else is just addition or subtraction in order to make everything line up correctly, it's like css but somehow even worse.
            }

            if (!buffer.contentsLost()) buffer.show(); // Theoretically since i'm not multithreading my buffer will never lose its contents, but just in case...

            graphics.dispose(); // We're done writing to this buffer, detach the graphics object from it now.
        }
    }

    // Returns an array containing the amount of times each letter was found, sorted from # of a's to # of z's.
    // For example a file containing "aAAa, BbB" would cause this method to return an array that looks like this:
    // {4, 3, 0, 0, 0...}
    public static int[] readFile() {

        try {
            File file = new File(filepath);
            Scanner reader = new Scanner(file, "UTF-8"); // This is how cool kids who want to do lots of unecessary extra work read files.
            // There was a pretty funny bug here where i guess the scanner assumes UTF-16 encoding or something and was reading an EOF in the middle of a file. Good times.

            int[] ret = new int[26];

            while (reader.hasNextLine()) {
                String line = reader.nextLine().toLowerCase(); // Make sure it's lowercase otherwise the capital letters don't count.
                for (int i = 0; i < line.length(); i++) {
                    int letter = (int)(line.charAt(i) - 'a'); // Gets the int code for whatever char we're looking at, and makes sure it aligns to the array by subtracting 'a'
                    if (letter > -1 && letter < ret.length) ret[letter]++; // We only want the chars from a-z, so disregard everything else. Otherwise, increase the count for whichever letter this is.
                }
            }

            reader.close(); // Save that sweet, sweet RAM by closing your file readers.
            return ret;

        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find specified file.");
            return null;
        }
    }
}