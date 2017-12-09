// This code deals with sorting a large file with limited RAM space on your system.
// It creates a large file containing texts and numbers. One per line.
// It then breaks the large file into chunks of size specified by the user
// It then sorts those chunks using heapsort and saves them to temp files
// Then it merges the tempfiles and produces a sorted file at the end
// The largefile, chunks and temp files created during the execution of this code gets deleted once
// it exits that function respectively.
// Only a sorted file called ResultingSortedfile-path

package shomronjacob;

import java.io.*;
import java.util.*;
import static java.util.concurrent.TimeUnit.NANOSECONDS;


public class heapsort 
{
	private static final int RAM_IN_MB =20; // defining the size of RAM available to sort
    private static final int LARGE_FILE_SIZE_IN_MB =100; // size of file to be created for sorting
    // use 200mb and 2GB to perform a the demo 
    //using 20MB and 100Gb here
    
    private static int random(int min, int max) // returns a random number between min and max
    {
        Random random = new Random();
        return min + random.nextInt(max - min + 1);
    }
    private static String randomString(int l) // returns string of length l
    {
        final String alphabet = "WORLDCUPISHERE2018";
        Random random = new Random();

        StringBuilder sb = new StringBuilder(l);
        for (int i = 0; i < l; i++) 
        {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
    
 // This function creates a large file of approximately mb megabytes size. Each line in the file contains
 //a String of length 10. The file gets deleted on exiting the code.
 // Returns the path of the file where it is saved temporarily
    private static String createLargeFileOfSize(int mb) throws IOException 
    {
        File file = File.createTempFile("largeDemofile", ".txt");
        file.deleteOnExit();

        // x MB = x * 1024 KB = x * 1024 * 1024 Bytes
        int sizeTarget = mb * 1024 * 1024;
        int sizeAvainBytes = 0;

        PrintWriter pw = new PrintWriter(new FileWriter(file));
        String line;
        do {
            line = randomString(random(3, 10));
            pw.println(line);
            sizeAvainBytes += line.length();
        } while (sizeAvainBytes < sizeTarget);

        pw.close();

        return file.getAbsolutePath();
    }
 // Writes all the lines created above to a .txt file and returns the path to the newly created file. 
 //The file gets deleted on exiting the code.
    
    private static String heapify(List<String> lines) throws IOException
    {
        File file = File.createTempFile("largeDemofile-chunk", ".txt");
        file.deleteOnExit();

        PrintWriter pw = new PrintWriter(new FileWriter(file));
        for (String line : lines) {
            pw.println(line);
        }
        pw.close();

        return file.getAbsolutePath();
    }
 //Reads the file in chunks of size MB and sorts each chunk and writes it to a temp file.
    private static List<String> readFileInChunksOfSize(int mb, String path) throws IOException 
    {
        File file = new File(path);
        List<String> chunkPaths = new ArrayList<String>();
        int sizeofchunks = mb * 1024 * 1024;
        int currentBytesRead = 0;

        List<String> lineList = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        for (String inline; (inline = br.readLine()) != null;) 
        {
            lineList.add(inline.intern());
            currentBytesRead += inline.length();
            if (currentBytesRead >= sizeofchunks) 
            {
                Collections.sort(lineList);
                chunkPaths.add(heapify(lineList));
                currentBytesRead = 0;
                lineList.clear();
            }
        }

        if (!lineList.isEmpty()) 
        {
            // Write remaining chunks
            Collections.sort(lineList);
            chunkPaths.add(heapify(lineList));
            lineList.clear();
        }
        br.close();

        return chunkPaths;
    }
// This function merges all the sorted chunks to form one main sorted file
// The sorted file is returned along with the path where it is saved
    
    private static String mergeChunks(List<String> chunkPaths) throws IOException
    {
        File resultFile = File.createTempFile("ResultingSortedfile", ".txt");
        PrintWriter resultPrintWriter = new PrintWriter(new FileWriter(resultFile));

        int numberOfChunks = chunkPaths.size();
        int bytesToReadPerChunk = (int) Math.ceil((RAM_IN_MB * 1024 * 1024) / (double) numberOfChunks);
        System.out.println("Number of chunks to be sorted are = " + numberOfChunks);
        System.out.println("bytesToReadPerChunk = " + bytesToReadPerChunk +" bytes");

        // Open a separate buffered reader for each chunk
        BufferedReader[] bufferedReaders = new BufferedReader[numberOfChunks];
        for (int c = 0; c < numberOfChunks; c++) {
            bufferedReaders[c] = new BufferedReader(new FileReader(new File(chunkPaths.get(c))));
        }
        List<String> lineList = new ArrayList<String>();
        String inline;
        int currentBytesRead;
        int finishedChunks = 0;
        while (finishedChunks < numberOfChunks) 
        {
            for (BufferedReader br : bufferedReaders) 
            {
                currentBytesRead = 0;

                BUFFERED_READER_LOOP:
                while ((inline = br.readLine()) != null)
                {
                    lineList.add(inline.intern());
                    currentBytesRead += inline.length();
                    if (currentBytesRead >= bytesToReadPerChunk) 
                    {
                        break BUFFERED_READER_LOOP;
                    }
                }

                if (inline == null)
                {
                    finishedChunks++;
                }
            }

            Collections.sort(lineList);
            for (String line : lineList) 
            {
                resultPrintWriter.println(line);
            }
            lineList.clear();
        }
// Close buffers
        for (BufferedReader br : bufferedReaders) 
        {
            br.close();
        }

        resultPrintWriter.close();
        return resultFile.getAbsolutePath();
    }
    
    // Main
    public static void main(String[] args) 
    {
        try {
        	final long startTime = System.nanoTime();
            String path = createLargeFileOfSize(LARGE_FILE_SIZE_IN_MB);
            System.out.println("Created file of size "+LARGE_FILE_SIZE_IN_MB+"MB at '"+path+"'");

            List<String> chunkPaths = readFileInChunksOfSize(RAM_IN_MB, path);
            String resultPath = mergeChunks(chunkPaths);
            long end= System.nanoTime();
            final long duration = end - startTime;
            System.out.println("Created sorted file at '"+resultPath+"'");
            System.out.println("The program takes "+NANOSECONDS.toSeconds(duration)+" seconds to run");
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }   
}

