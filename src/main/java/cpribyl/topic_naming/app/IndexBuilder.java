package cpribyl.topic_naming.app;

import cpribyl.topic_naming.car.CarIndex;
import cpribyl.topic_naming.index.Index;

import java.util.List;

public class IndexBuilder {
  public static Index loadCar(String indexDir) {
    return CarIndex.load(indexDir).get();
  }
  public static Index createCar(String indexDir, List<String> jsonlFiles) {
    return CarIndex.fromJsonls(indexDir, jsonlFiles).get();
  }
}
