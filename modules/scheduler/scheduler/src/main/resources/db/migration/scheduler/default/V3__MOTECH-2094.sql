-- change job_class_name value for jobs created before scheduler API clean up --
UPDATE "qrtz_job_details" SET "job_class_name"='org.motechproject.scheduler.service.MotechScheduledJob' WHERE "job_class_name" LIKE 'org.motechproject.scheduler.service.impl.MotechScheduledJob'