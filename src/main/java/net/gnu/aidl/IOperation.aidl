package net.gnu.aidl;

interface IOperation {
  //String[] readWordFileToParagraphs( in String fPath );
  // String readWorkBook( in String fPath, in int colNum, in boolean manySheet, in String outFolder );
  void pdfToText(in String fPath, in String fOutPath);
  //String readWordFileToText(in String fPath);
  //String getPublisherText(in String fPath);
  //String getVisioText(in String fPath);
  //String getPowerPointText(in String fPath);
  //String getExcelText(in String fPath);
  
  //String readGenExcelDict(boolean manySheet);
  //String readOriExcelDict();
  //void writeNewWordsSheet(String dicFileNameSer, String outputFile, String sheetName);

}

