# minas-reference-implementation

Reference implementation for MINAS (MultI-class learNing Algorithm for data Streams), an algorithm to address novelty detection in data streams multi-class problems.

Faria, E. R., Gama, J., & Carvalho, A. C. (2013, March). Novelty detection algorithm for data streams multi-class problems. In Proceedings of the 28th annual ACM symposium on applied computing (pp. 795-800). https://doi.org/10.1145/2480362.2480515

de Faria, E.R., Ponce de Leon Ferreira Carvalho, A.C. & Gama, J. MINAS: multiclass learning algorithm for novelty detection in data streams. Data Min Knowl Disc 30, 640–680 (2016). https://doi.org/10.1007/s10618-015-0433-y

## Documentation

The documentation of this project is available in the following link:
https://douglas444.github.io/minas-reference-implementation/

## Requirements

* Java 7 or higher.

## How to compile

First delete the content from the bin folder.
Once the bin folder is empty, execute the following command from the root of the project:

```
javac src/br/ufu/facom/minas/core/datastructure/*.java src/br/ufu/facom/minas/core/decisionrule/*/*.java src/br/ufu/facom/minas/core/decisionrule/*.java src/br/ufu/facom/minas/core/clustering/*.java src/br/ufu/facom/minas/core/*.java src/br/ufu/facom/minas/example/*.java -d bin
```

## How to run

To run the MOA3 example, execute the following command from the root of the project:

```
java -cp bin br.ufu.facom.minas.example.MOA3
```
To run the SynEDC example, execute the following command from the root of the project:

```
java -cp bin br.ufu.facom.minas.example.SynEDC
```
To run the SynD example, execute the following command from the root of the project:

```
java -cp bin br.ufu.facom.minas.example.SynD
```
To run the KDD99 example, execute the following command from the root of the project:

```
java -cp bin br.ufu.facom.minas.example.KDD99
```
To run the covtype example, execute the following command from the root of the project:

```
java -cp bin br.ufu.facom.minas.example.covtype
```

## How to generate the javadocs

Execute the following command from the root of the project:
```
javadoc -sourcepath src -d docs -subpackages .
```
