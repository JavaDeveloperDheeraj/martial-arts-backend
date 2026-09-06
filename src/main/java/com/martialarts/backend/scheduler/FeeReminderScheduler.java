package com.martialarts.backend.scheduler;

import com.martialarts.backend.dto.FeeLedgerDTO;
import com.martialarts.backend.entity.Student;
import com.martialarts.backend.repository.StudentRepository;
import com.martialarts.backend.service.FeesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@EnableScheduling
public class FeeReminderScheduler {

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private FeesService feesService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void runDailyFeeCheck() {
        System.out.println("============== 🔔 RUNNING DAILY FEE REMINDER CRON JOB ==============");
        List<Student> approvedStudents = studentRepo.findByStatus("APPROVED");

        LocalDate today = LocalDate.now();

        for (Student s : approvedStudents) {
            try {
                FeeLedgerDTO ledger = feesService.getComprehensiveLedger(s.getId());
                LocalDate nextDue = ledger.getNextDueDate();

                if (nextDue != null) {
                    long daysUntilDue = ChronoUnit.DAYS.between(today, nextDue);

                    if (daysUntilDue > 0 && daysUntilDue <= 10) {
                        System.err.println(String.format("📢 [SMS/CONSOLE REMINDER] Dear %s (Mob: %s), your Academy fee of ₹%.2f is due on %s (%d Days Remaining). Kindly pay on time.",
                                s.getName(), s.getMobile(), ledger.getNextDueAmount(), nextDue, daysUntilDue));
                    } else if (daysUntilDue == 0) {
                        System.err.println(String.format("🚨 [TODAY DUE] Dear %s, your Academy fee of ₹%.2f is DUE TODAY (%s)!",
                                s.getName(), ledger.getNextDueAmount(), today));
                    } else if (daysUntilDue < 0 && ledger.getNetOutstanding() > 0) {
                        System.err.println(String.format("⚠️ [OVERDUE ALERT] Dear %s, your Academy fee of ₹%.2f is OVERDUE by %d days! Please clear immediately.",
                                s.getName(), ledger.getNetOutstanding(), Math.abs(daysUntilDue)));
                    }
                }
            } catch (Exception e) {
                // Ignore for students without fee plan
            }
        }
        System.out.println("====================================================================");
    }
}