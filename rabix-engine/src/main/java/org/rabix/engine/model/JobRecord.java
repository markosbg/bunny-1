package org.rabix.engine.model;

import java.util.ArrayList;
import java.util.List;

import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.service.JobRecordService.JobState;

public class JobRecord {

  private final String id;
  private final String externalId;
  private final String rootId;
  private final String parentId;
  private boolean blocking;
  
  private JobState state;
  
  private List<PortCounter> inputCounters;
  private List<PortCounter> outputCounters;

  private boolean isScattered;                  // it's created from scatter
  private boolean isContainer;                  // it's a container Job
  private boolean isScatterWrapper;             // it's a scatter wrapper

  private int numberOfGlobalInputs = 0;
  private int numberOfGlobalOutputs = 0;
  
  private ScatterStrategy scatterStrategy;
  
  public JobRecord(String rootId, String id, String uniqueId, String parentId, JobState state, boolean isContainer, boolean isScattered, boolean blocking) {
    this.id = id;
    this.externalId = uniqueId;
    this.rootId = rootId;
    this.parentId = parentId;
    this.state = state;
    this.blocking = blocking;
    this.isContainer = isContainer;
    this.isScattered = isScattered;
    this.inputCounters = new ArrayList<>();
    this.outputCounters = new ArrayList<>();
  }
  
  public boolean isRoot() {
    return externalId.equals(rootId);
  }
  
  public boolean isBlocking() {
    return blocking;
  }

  public void setBlocking(boolean blocking) {
    this.blocking = blocking;
  }

  public JobState getState() {
    return state;
  }

  public void setState(JobState state) {
    this.state = state;
  }

  public List<PortCounter> getInputCounters() {
    return inputCounters;
  }

  public void setInputCounters(List<PortCounter> inputCounters) {
    this.inputCounters = inputCounters;
  }

  public List<PortCounter> getOutputCounters() {
    return outputCounters;
  }

  public void setOutputCounters(List<PortCounter> outputCounters) {
    this.outputCounters = outputCounters;
  }

  public boolean isScattered() {
    return isScattered;
  }

  public void setScattered(boolean isScattered) {
    this.isScattered = isScattered;
  }

  public boolean isContainer() {
    return isContainer;
  }

  public void setContainer(boolean isContainer) {
    this.isContainer = isContainer;
  }

  public boolean isScatterWrapper() {
    return isScatterWrapper;
  }

  public void setScatterWrapper(boolean isScatterWrapper) {
    this.isScatterWrapper = isScatterWrapper;
  }

  public int getNumberOfGlobalInputs() {
    return numberOfGlobalInputs;
  }

  public void setNumberOfGlobalInputs(int numberOfGlobalInputs) {
    this.numberOfGlobalInputs = numberOfGlobalInputs;
  }

  public int getNumberOfGlobalOutputs() {
    return numberOfGlobalOutputs;
  }

  public void setNumberOfGlobalOutputs(int numberOfGlobalOutputs) {
    this.numberOfGlobalOutputs = numberOfGlobalOutputs;
  }

  public ScatterStrategy getScatterStrategy() {
    return scatterStrategy;
  }

  public void setScatterStrategy(ScatterStrategy scatterStrategy) {
    this.scatterStrategy = scatterStrategy;
  }

  public String getId() {
    return id;
  }

  public String getExternalId() {
    return externalId;
  }

  public String getRootId() {
    return rootId;
  }

  public String getParentId() {
    return parentId;
  }

  public static class PortCounter {
    private String port;
    private int counter;
    private boolean scatter;
    
    private int incoming;

    public PortCounter(String port, int counter, boolean scatter) {
      this.port = port;
      this.counter = counter;
      this.scatter = scatter;
      this.incoming = 0;
    }

    public void increaseIncoming() {
      this.incoming++;
    }
    
    public String getPort() {
      return port;
    }

    public void setPort(String port) {
      this.port = port;
    }

    public int getCounter() {
      return counter;
    }

    public void setCounter(int counter) {
      this.counter = counter;
    }

    public boolean isScatter() {
      return scatter;
    }

    public void setScatter(boolean scatter) {
      this.scatter = scatter;
    }

    public int getIncoming() {
      return incoming;
    }

    public void setIncoming(int incoming) {
      this.incoming = incoming;
    }
  }

  @Override
  public String toString() {
    return "JobRecord [id=" + id + ", externalId=" + externalId + ", rootId=" + rootId + ", state=" + state + ", inputCounters=" + inputCounters + ", outputCounters=" + outputCounters + ", isScattered=" + isScattered + ", isContainer=" + isContainer + ", isScatterWrapper=" + isScatterWrapper + ", numberOfGlobalInputs=" + numberOfGlobalInputs + ", numberOfGlobalOutputs=" + numberOfGlobalOutputs + ", scatterStrategy=" + scatterStrategy + "]";
  }

}
