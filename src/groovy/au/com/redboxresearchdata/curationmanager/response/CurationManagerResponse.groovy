package au.com.redboxresearchdata.curationmanager.response

class CurationManagerResponse {

	Long jobId;
	String jobStatus;
	Date dateCreated;
	Date dateCompleted;
	List jobItems 	
	
	public void setJobId(Long jobId){
		this.jobId = jobId;
	}
	
	public Long getJobId(){
		return jobId;
	}
	
	public void setJobStatus(String jobStatus){
		this.jobStatus = jobStatus;
	}
	
	public String getJobStatus(){
		return jobStatus;
	}
	
	public void setDateCompleted(Date dateCompleted){
		this.dateCompleted = dateCompleted;
	}
	
	public Date getDateCompleted(){
		return dateCompleted;
	}
	
	public void setJobItems(List jobItems){
		this.jobItems = jobItems;
	}
	
	public List getJobItems(){
		return jobItems;
	}	
}