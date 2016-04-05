import numpy as np
import matplotlib.pyplot as plt

data = np.genfromtxt('generation.csv', delimiter=',')

index = []
for i in range(0,len(data)):
    index.append(i)

plt.plot(index,data)
plt.title('learning')
plt.xlim(xmin=0)
plt.ylim(ymin=0)
plt.xlabel('generations')
plt.ylabel('score')
plt.show()
