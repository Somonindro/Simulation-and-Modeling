import numpy as np

# %%
number_of_simulations = 10000
number_of_people = 100

# %%
ranks = np.arange(1, number_of_people + 1)
np.random.shuffle(ranks)
print(ranks, ranks.shape)

# %%
success_count = {
    "top1": np.zeros(number_of_people),
    "top3": np.zeros(number_of_people),
    "top5": np.zeros(number_of_people),
    "top10": np.zeros(number_of_people)
}

# %%
import sys

# Open the output file
f = open('output.txt', 'w')
    # Redirect stdout to the file
sys.stdout = f

# %%
for sim_id in range(number_of_simulations):
    np.random.shuffle(ranks)
    # print(sim_id)
    # print(ranks)
    top1 = np.argpartition(ranks, 1)[:1]
    top3 = np.argpartition(ranks, 3)[:3]
    top5 = np.argpartition(ranks, 5)[:5]
    top10 = np.argpartition(ranks, 10)[:10]
    # print(ranks)
    top_combined = np.concatenate((top1, top3, top5, top10))
    # print(top1, top3, top5, top10)
    # print(top_combined)
    for subset_index in range(0, number_of_people):
        min_rank = 100
        subset = ranks[:subset_index]
        min_rank = min(subset) if subset.size > 0 else min_rank
        remaining = ranks[subset_index:]
        # find the 1st index of the elem < min_rank
        indices = np.where(remaining < min_rank)
        # print(indices)
        min_index = subset_index
        if indices[0].size > 0:
            min_index = indices[0][0] + subset_index
        # else:
        #     min_index = subset_index if remaining.size > 0 else min_index
        
        counts = np.isin(top_combined, min_index).sum()
        # if subset_index == number_of_people - 1:
        #     print("min" , min_index)
        #     print("counts", counts)
        #     print(ranks)
        #     print(top_combined)
        # print(np.isin(min_index, top10).sum(), counts, np.cumsum(counts))
        success_count["top1"][subset_index] += (counts == 4)
        success_count["top3"][subset_index] += (counts >= 3)
        success_count["top5"][subset_index] += (counts >= 2)
        success_count["top10"][subset_index] += (counts >= 1)

# %%
sys.stdout = sys.__stdout__
success_count