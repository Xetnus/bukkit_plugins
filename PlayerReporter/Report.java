package me.Xetnus.PlayerReporter;

import org.bukkit.entity.Player;
import java.util.Date;

public class Report
{
	private Player reportedBy;
	private String accusedWhom, reason;
	private Date dateOfReport;
	
	public Report(Player reportedBy, String accusedWhom, String reason)
	{
		this.reportedBy = reportedBy;
		this.accusedWhom = accusedWhom;
		this.reason = reason;
		Date date = new Date();
		this.dateOfReport = date;
	}
	
	public void displayDateAndTime()
	{
		reportedBy.sendMessage("Date: " + dateOfReport);
	}
	
	public Date getDate()
	{
		return dateOfReport;
	}
	
	public String getReportedBy()
	{
		return reportedBy.getName();
	}
	
	public String getAccusedWhom()
	{
		return accusedWhom;
	}
	
	@SuppressWarnings("deprecation")
	public boolean checkCooldownBetweenSameReport(Report previousReport)
	{
		Date previousDate = previousReport.getDate();
		
		if (previousDate.getYear() < dateOfReport.getYear())
			return true;
		else if (previousDate.getYear() == dateOfReport.getYear() && previousDate.getMonth() < dateOfReport.getMonth())
			return true;
		else if (previousDate.getYear() == dateOfReport.getYear() && previousDate.getMonth() == dateOfReport.getMonth() && previousDate.getDay() < dateOfReport.getDay())
			return true;
		else if (previousDate.getYear() == dateOfReport.getYear() && previousDate.getMonth() == dateOfReport.getMonth() && previousDate.getDay() == dateOfReport.getDay())
		{
			if (dateOfReport.getHours() - previousDate.getHours() >= 1)
				return true;
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean checkCooldownBetweenAllReports(Report previousReport)
	{
		Date previousDate = previousReport.getDate();
		
		if (previousDate.getYear() < dateOfReport.getYear())
			return true;
		else if (previousDate.getYear() == dateOfReport.getYear() && previousDate.getMonth() < dateOfReport.getMonth())
			return true;
		else if (previousDate.getYear() == dateOfReport.getYear() && previousDate.getMonth() == dateOfReport.getMonth() && previousDate.getDay() < dateOfReport.getDay())
			return true;
		else if (previousDate.getYear() == dateOfReport.getYear() && previousDate.getMonth() == dateOfReport.getMonth() && previousDate.getDay() == dateOfReport.getDay() && previousDate.getHours() < dateOfReport.getHours())
			return true;
		else if (previousDate.getYear() == dateOfReport.getYear() && previousDate.getMonth() == dateOfReport.getMonth() && previousDate.getDay() == dateOfReport.getDay() && previousDate.getHours() == dateOfReport.getHours())
		{
			if (dateOfReport.getMinutes() - previousDate.getMinutes() >= 2)
				return true;
		}
		
		return false;
	}
}
