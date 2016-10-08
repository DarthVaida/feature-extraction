from numpy import *
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.metrics.pairwise import cosine_similarity


t = TfidfTransformer()
X = genfromtxt('/home/hexd123/workspace/feature-extraction/output/data.txt', unpack=True).transpose()

print("The data:")
print X 
tfidf = t.fit_transform(X)
cos = cosine_similarity(tfidf[0], tfidf)
print("Path similarity:"),
for e in cos:
	for i in e:
		print i, 
		print "::",


