let numberOfGenerations, numberOfSimulations, maxNumberOfNeutrons, maxNewGenerationToBeConsidered, countsForEachGeneration;


function randomChoiceWithProb(arr, prob) {
  let sum = prob.reduce((a, b) => a + b, 0);
  let acc = 0;
  prob = prob.map(p => (acc = p + acc));
  let rand = Math.random() * sum;
  return arr[prob.filter(p => p <= rand).length];
}


function initialize() {
  numberOfGenerations = 10;
  numberOfSimulations = 10000;
  maxNumberOfNeutrons = 3;
  maxNewGenerationToBeConsidered = 4;
  countsForEachGeneration = Array(numberOfGenerations).fill().map(() => Array(maxNewGenerationToBeConsidered + 1).fill(0));
}

function getProbabilities(i) {
  return 0.2126 * Math.pow(0.5893, i - 1);
}

initialize();

let p = Array.from({length: maxNumberOfNeutrons}, (_, i) => getProbabilities(i + 1));
p.unshift(1 - p.reduce((a, b) => a + b, 0));

// console.log(p)

// initialize() 

for (let simId = 0; simId < numberOfSimulations; simId++) {
  let numberOfNeutrons = 1;
  for (let generationId = 0; generationId < numberOfGenerations; generationId++) {
    let newNeutrons = 0;
    for (let i = 0; i < numberOfNeutrons; i++) {
      let arr = Array.from({length: maxNumberOfNeutrons + 1}, (_, i) => i);
      newNeutrons += randomChoiceWithProb(arr, p);
    }
    if (newNeutrons <= maxNewGenerationToBeConsidered) {
      countsForEachGeneration[generationId][newNeutrons]++;
    }
    numberOfNeutrons = newNeutrons;
  }
}

countsForEachGeneration = countsForEachGeneration.map(subArray =>
  subArray.map(value => value / numberOfSimulations)
);


for (let generation in countsForEachGeneration) {
  console.log("Generation "+ generation + ":");
  countsForEachGeneration[generation].forEach((value, i) => {
      console.log(`p[${i}] = ${value}`);
  });
  console.log();
}

// console.log(countsForEachGeneration)