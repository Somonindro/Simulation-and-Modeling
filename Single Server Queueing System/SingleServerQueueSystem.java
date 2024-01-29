import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class SingleServerQueueSystem {
    private final int Q_LIMIT;
    private final int BUSY;
    private final int IDLE;

    private int count,customerCounter,nextEventType, numCustomersDelayed, numDelaysRequired, numEvents, numInQueue, serverStatus;
    private float areaNumInQueue, areaServerStatus, meanInterarrival, meanService, simTime, timeLastEvent;

    private float[] timeArrival;
    private float[] timeNextEvent;
    private float totalOfDelays;
    private FileWriter fileWriter;
    private FileWriter outfile;

    public SingleServerQueueSystem(FileWriter outfile,float meanInterarrival,float meanService,int numDelaysRequired) throws IOException
    {
        Q_LIMIT = 100;
        BUSY = 1;
        IDLE = 0;
        timeArrival = new float[Q_LIMIT + 1];
        timeNextEvent = new float[3];

        this.meanInterarrival = meanInterarrival;
        this.meanService = meanService;
        this.numDelaysRequired = numDelaysRequired;
        this.outfile = outfile;

        count=1;
        customerCounter = 1;
        totalOfDelays = 0.0f;
        areaNumInQueue = 0.0f;
        areaServerStatus = 0.0f;
        simTime = 0.0f;
        serverStatus = IDLE;
        numInQueue = 0;
        timeLastEvent = 0.0f;
        numCustomersDelayed = 0;
        numEvents = 2;
        timeNextEvent[1] = simTime + expon(meanInterarrival);
        timeNextEvent[2] = 1.0e+30f;

        fileWriter = new FileWriter("event_orders.txt");

    }

    public float getAvgDelay()
    {
        return (totalOfDelays / numCustomersDelayed);
    }

    public float getAvgNumInQueue()
    {
        return (areaNumInQueue / simTime);
    }

    public float getServerUtilization()
    {
        return (areaServerStatus / simTime);
    }

    public float getSimTime()
    {
        return simTime;
    }

    public float expon(float mean) 
    {
        return (float) (-mean * Math.log(Generator.lcgrand(1)));
    }

    public void timing() throws IOException 
    {
        float minTimeNextEvent = 1.0e+29f;
        nextEventType = 0;

        for (int i = 1; i <= numEvents; i++) 
        {
            if (timeNextEvent[i] < minTimeNextEvent) 
            {
                minTimeNextEvent = timeNextEvent[i];
                nextEventType = i;
            }
        }

        if (nextEventType == 0) 
        {
            outfile.write("\nEvent list empty at time " + simTime);
            outfile.close();
            System.exit(1);
        }

        simTime = minTimeNextEvent;
    }


    public void customer_arrival() throws IOException 
    {
        timeNextEvent[1] = simTime + expon(meanInterarrival);
        if(serverStatus == IDLE)
        {
            ++numCustomersDelayed;
            fileWriter.write("\n\n---------No. of customers delayed: " + numCustomersDelayed + "--------\n\n");
            timeNextEvent[2] = simTime + expon(meanService);
            serverStatus = BUSY;
        }
        else 
        {
            ++numInQueue;
            if (numInQueue > Q_LIMIT) 
            {
                outfile.write("\nOverflow of the array at time " + simTime);
                outfile.close();
                System.exit(2);
            }
            timeArrival[numInQueue] = simTime;
        } 
    }

    public void customer_departure() throws IOException 
    {
        if (numInQueue == 0) 
        {
            serverStatus = IDLE;
            timeNextEvent[2] = 1.0e+30f;
        } 
        else 
        {
            --numInQueue;
            totalOfDelays += (simTime - timeArrival[1]);
            ++numCustomersDelayed;
            fileWriter.write("\n\n---------No. of customers delayed: " + numCustomersDelayed + "--------\n\n");
            timeNextEvent[2] = simTime + expon(meanService);
            for (int i = 1; i <= numInQueue; i++) 
            {
                timeArrival[i] = timeArrival[i + 1];
            }
        }
    }

    public void updateTimeAvgStats() 
    {
        float timeSinceLastEvent = simTime - timeLastEvent;
        timeLastEvent = simTime;
        areaNumInQueue += numInQueue * timeSinceLastEvent;
        areaServerStatus += serverStatus * timeSinceLastEvent;
    }

    public void runSimulation() throws IOException 
    {
        while (numCustomersDelayed < numDelaysRequired) 
        {
            timing();
            updateTimeAvgStats();

            if(nextEventType == 1)
            {
                fileWriter.write(count + ". Next event: Customer " + customerCounter + " Arrival\n");
                count++;
                customerCounter++;
                customer_arrival();
            }
            else if(nextEventType == 2)
            {
                fileWriter.write(count + ". Next event: Customer " + numCustomersDelayed + " Departure\n");
                count++;
                customer_departure();
            }
        }

        fileWriter.close();
    }

    public static void main(String[] args) throws IOException {

        FileWriter outfile = new FileWriter("results.txt");

        File inputFile = new File("in.txt");
        Scanner fileScanner = new Scanner(inputFile);

        float meanInterarrival=(float)fileScanner.nextDouble();
        float meanService=(float)fileScanner.nextDouble();
        int numDelaysRequired= fileScanner.nextInt();

        outfile.write("----Single-server queueing system----\n\n");
        outfile.write("Mean interarrival time: " + meanInterarrival + " minutes\n");
        outfile.write("Mean service time: " + meanService + " minutes\n");
        outfile.write("Number of customers: " + numDelaysRequired+"\n\n\n");

        SingleServerQueueSystem system = new SingleServerQueueSystem(outfile,meanInterarrival,meanService,numDelaysRequired);
        system.runSimulation();

        outfile.write("Average delay in queue: " + system.getAvgDelay() + " minutes\n");
        outfile.write("Average number in queue: " + system.getAvgNumInQueue()+"\n");
        outfile.write("Server utilization: " + system.getServerUtilization()+"\n");
        outfile.write("Time simulation ended: " + system.getSimTime() + " minutes\n");

        outfile.close();
        fileScanner.close(); 
    }
}

