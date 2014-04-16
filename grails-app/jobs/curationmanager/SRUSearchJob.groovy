package curationmanager

import au.com.redboxresearchdata.curationmanager.jobrunner.CurationJobRunner;

class SRUSearchJob {
	CurationJobRunner curationJobRunner = new CurationJobRunner();
    static triggers = {
      simple repeatInterval: 30000l // execute job once in 30 seconds
    }

    def execute() {
		curationJobRunner.executeJob();
    }
}
