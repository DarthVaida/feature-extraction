from numpy import *
from sklearn import svm
from sklearn import datasets
clf = svm.SVC(gamma=0.001, C=100)

X = genfromtxt('data.txt', unpack=True).transpose()
print (X)

y =array([[1,1,1,2,1]]).reshape(-1,1).ravel()
print(y)

clf.fit(X, y)  

print(clf.predict(X[-1:]))
