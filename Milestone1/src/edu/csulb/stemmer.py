import sys

from porter2stemmer import Porter2Stemmer
stemmer = Porter2Stemmer()
print(stemmer.stem(sys.argv[1]))
