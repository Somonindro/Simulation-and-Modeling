import java.io.*;
import java.util.Scanner;

public class InventorySystem {

    private int initialInventoryLevel, currentInventoryLevel, totalMonths, totalPolicies;
    private int s, S, amount, nextEventType, numEvents, numDemandSize;
    private int[] small_S, capital_S;

    private double holdingCost, incrementalCost, setupCost, shortageCost, totalOrderingCost;
    private double minLag, maxLag;
    private double areaHolding, areaShortage, meanInterDemandTime, simulationTime, timeLastEvent;
    private double[] probabilityDistributionDemand, timeNextEvent;

    private Scanner infile;
    private FileWriter outfile;
    private double avgHoldingCost, avgOrderingCost, avgShortageCost, avgTotalCost;

    public InventorySystem(Scanner infile, FileWriter outfile) throws IOException {

        this.infile = infile;
        this.outfile = outfile;
        numEvents = 4;

        // The first line contains Initial Inventory Level, Total Number of Months and
        // Number of Policies.
        initialInventoryLevel = infile.nextInt();
        totalMonths = infile.nextInt();
        totalPolicies = infile.nextInt();

        // The next line contains The Number of Demand Sizes and The Mean Inter-demand
        // Time in months.
        numDemandSize = infile.nextInt();
        meanInterDemandTime = infile.nextDouble();

        // The next line contains the Setup Cost and per-unit Incremental Cost, Holding
        // Cost & Shortage Cost.
        setupCost = infile.nextDouble();
        incrementalCost = infile.nextDouble();
        holdingCost = infile.nextDouble();
        shortageCost = infile.nextDouble();

        // The next would have Minlag and Maxlag periods in months.
        minLag = infile.nextDouble();
        maxLag = infile.nextDouble();

        // The next line has the cumulative probabilities of the sequential demand sizes
        // (i.e. for demand sizes of 1, 2, …, D).
        probabilityDistributionDemand = new double[numDemandSize + 1];
        for (int i = 1; i <= numDemandSize; i++) {
            probabilityDistributionDemand[i] = infile.nextFloat();
        }

        small_S = new int[totalPolicies];
        capital_S = new int[totalPolicies];

        // The next P lines each has 2 space-separated numbers s, S denoting the
        // respective policies.
        for (int i = 0; i < totalPolicies; i++) {
            small_S[i] = infile.nextInt();
            capital_S[i] = infile.nextInt();
        }

        infile.close();

    }

    private void initialization() {

        simulationTime = 0.0f;
        currentInventoryLevel = initialInventoryLevel;
        timeLastEvent = 0.0f;
        totalOrderingCost = 0.0f;
        areaHolding = 0.0f;
        areaShortage = 0.0f;

        timeNextEvent = new double[5];
        timeNextEvent[1] = 1.0e+30f;
        timeNextEvent[2] = simulationTime + Generator.expon(meanInterDemandTime);
        timeNextEvent[3] = totalMonths;
        timeNextEvent[4] = 0.0f;

    }

    public double getAvgHoldingCost() {
        avgHoldingCost = holdingCost * areaHolding / totalMonths;
        return avgHoldingCost;
    }

    public double getAvgOrderingCost() {
        avgOrderingCost = totalOrderingCost / totalMonths;
        return avgOrderingCost;
    }

    public double getAvgShortageCost() {
        avgShortageCost = shortageCost * areaShortage / totalMonths;
        return avgShortageCost;
    }

    public double getAvgTotalCost() {
        avgTotalCost = getAvgHoldingCost() + getAvgOrderingCost() + getAvgShortageCost();
        return avgTotalCost;
    }

    private void orderArrival() {
        currentInventoryLevel += amount;
        timeNextEvent[1] = 1.0e+30f;
    }

    private void demand() {
        currentInventoryLevel -= Generator.randomInteger(probabilityDistributionDemand);
        timeNextEvent[2] = simulationTime + Generator.expon(meanInterDemandTime);
    }

    private void evaluate() {
        if (currentInventoryLevel < s) {
            amount = S - currentInventoryLevel;
            totalOrderingCost += setupCost + incrementalCost * amount;
            timeNextEvent[1] = simulationTime + Generator.uniform(minLag, maxLag);
        }
        timeNextEvent[4] = simulationTime + 1.0f;
    }

    private void updateTimeAvgStatistics() {
        if (currentInventoryLevel > 0) {
            areaHolding += currentInventoryLevel * (simulationTime - timeLastEvent);
        } else if (currentInventoryLevel < 0) {
            areaShortage -= currentInventoryLevel * (simulationTime - timeLastEvent);
        }
        timeLastEvent = simulationTime;
    }

    private void printParams() throws IOException {

        outfile.write("------Single-Product Inventory System------\n\n");
        outfile.write("Initial inventory level: " + initialInventoryLevel + " items\n");
        outfile.write("Number of demand sizes: " + numDemandSize + "\n");
        outfile.write("Distribution function of demand sizes: ");

        for (int i = 1; i <= numDemandSize; i++) {
            outfile.write(probabilityDistributionDemand[i] + "  ");
        }

        outfile.write("\n\nMean inter-demand time: " + meanInterDemandTime + " months\n");
        outfile.write("Delivery lag range: " + minLag + " to " + maxLag + " months\n");
        outfile.write("Length of the simulation: " + totalMonths + " months\n\n");
        outfile.write("Costs:\n");
        outfile.write("K =" + setupCost + "\ni =" + incrementalCost + "\nh =" + holdingCost + "\npi =" + shortageCost);
        outfile.write("\nNumber of policies: " + totalPolicies);

        outfile.write(" \n---------------------------------------------------------------------------------\n");
        outfile.write(" Policy\tAvg_total_cost\tAvg_ordering_cost\tAvg_holding_cost\tAvg_shortage_cost\n");
        outfile.write("----------------------------------------------------------------------------------\n");
    }

    private void timing() {
        double min_time = 1.0e+30f;
        int min_event = 0;

        for (int i = 1; i <= numEvents; i++) {
            if (timeNextEvent[i] < min_time) {
                min_time = timeNextEvent[i];
                min_event = i;
            }
        }

        simulationTime = min_time;
        nextEventType = min_event;
    }

    private void simulate() throws IOException {
        printParams();
        for (int i = 0; i < totalPolicies; i++) {
            s = small_S[i];
            S = capital_S[i];
            initialization();
            do {
                timing();
                updateTimeAvgStatistics();
                if (nextEventType == 1) {
                    orderArrival();
                } else if (nextEventType == 2) {
                    demand();
                } else if (nextEventType == 3) {
                    String formattedString = String.format("\n(%d, %d)\t\t%.2f\t\t\t%.2f\t\t\t%.2f\t\t\t%.2f\n", s, S,
                            getAvgTotalCost(), getAvgOrderingCost(), getAvgHoldingCost(), getAvgShortageCost());
                    outfile.write(formattedString);
                } else {
                    evaluate();
                }
            } while (nextEventType != 3);
        }
        outfile.write("\n---------------------------------------------------------------------------------------\n");
        outfile.close();
    }

    public static void main(String[] args) throws IOException {
        Scanner infile = new Scanner(new File("in.txt"));
        FileWriter outfile = new FileWriter("results.txt");
        InventorySystem inventorySystem = new InventorySystem(infile, outfile);
        inventorySystem.simulate();
        infile.close();
        outfile.close();
    }
}