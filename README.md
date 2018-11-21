BUILD, TEST and EXECUTION INSTRUCTIONS:
The project is a Spring boot project. And, maven is used as build tool.

1. Open terminal
2. Go to project directory
3. Run 'mvn clean' command to clean the projet
4. Run 'mvn clean install -DskipTests' command to BUILD without TEST.
5. Run 'mvn clean install' command to BUILD the project with TEST.
6. Execute 'java -jar target/fastafileprocessor-0.0.1-SNAPSHOT.jar sample.fa.gz sample1.fa.gz --thread-number=16' to RUN the program.
7. The program will generate REPORT.TXT and SEQUENCE.FASTA.GZ as per Programming task general instructions.

EXPLAINATIN OF PRGRAM BEHAVIOR FOR INCORRECT INPUT:
If invalid input parameters passed:
1. input validation is checked by ArgumentsValidator class.
2. IllegalArgumentException is thrown when invalid parameters are passed.
3. ArgumentsValidatorUnitTest has some tests to check this specific case.

If invalid input files are provided:
1. GZipFiles class throws InvalidGzipFormatException if the input files are not gzipped.
2. GZipFilesUnitTest has some tests to check no. 1 case.
3. FastaFileReader class throws InvalidFastaFormatException if the input files are not Fasta files.
4. FastaFileReaderUnitTest has test to check this no. 3 case.

DESCRIPTION:
This project is particularly challenging one when large files need to be processed. 
Two things need to be addressed. One is use of multi-threading
to shorten the processing time. Second is minimize loading data into RAM to
avoid Out of Memory state. To generate the concatenated sequence file all the sequences' positions
are read in first turn and then the sequences are read from the file directly and write it to the 
output file sequentially without caching.

MULTITHREADING:
1. ConcurrentHashMap collection, AtomicLong
2. compute method of ConcurrentHashMap is used for atomic operation

DESIGN PATTERN:
1. Builder Pattern
2. Dependency Injection