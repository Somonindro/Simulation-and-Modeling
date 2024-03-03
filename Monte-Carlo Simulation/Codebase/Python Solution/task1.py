import numpy as np

def initialize():
  global number_of_generations, number_of_simulations, max_number_of_neutrons, max_new_generation_to_be_considered, counts_for_each_generation
  number_of_generations = 10
  number_of_simulations = 10000
  max_number_of_neutrons = 3
  max_new_generation_to_be_considered = 4
  counts_for_each_generation = np.zeros((number_of_generations, max_new_generation_to_be_considered + 1))

# %%
def get_probabilities(i):
  return (0.2126)*(0.5893)**(i - 1)

# %%
p = np.array([get_probabilities(i) for i in range(1, max_number_of_neutrons + 1)])
p = np.insert(p, 0, 1 - np.sum(p))
print(p)

initialize()

for sim_id in range(number_of_simulations):
  number_of_neutrons = 1
  for generation_id in range(number_of_generations):
    new_neutrons = 0
    for i in range(number_of_neutrons):
      new_neutrons += np.random.choice(np.arange(0, max_number_of_neutrons + 1), p=p)
    if new_neutrons <= max_new_generation_to_be_considered:
      counts_for_each_generation[generation_id][new_neutrons] += 1
    number_of_neutrons = new_neutrons




