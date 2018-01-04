package main.java.net.bdew.wurm.hwportals.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.villages.Village;
import main.java.net.bdew.wurm.hwportals.PortalTracker;

import main.java.net.bdew.wurm.hwportals.TeleportHelper;
import org.gotti.wurmunlimited.modsupport.questions.ModQuestion;
import org.gotti.wurmunlimited.modsupport.questions.ModQuestions;

import java.util.List;
import java.util.Properties;

public class TeleportQuestion implements ModQuestion {
    private final Creature performer;
    private final List<Village> targets;

    private TeleportQuestion(Creature performer, List<Village> targets) {
        this.performer = performer;
        this.targets = targets;
    }

    @Override
    public void sendQuestion(Question question) {
        final StringBuilder buf = new StringBuilder(ModQuestions.getBmlHeader(question));
        buf.append("text{text='Choose your destination:.'}text{text=''}");

        buf.append("dropdown{id='tgt';options='");
        targets.forEach(v -> buf.append((v.isPermanent || v.isCapital()) ? v.getName() + " (Spawn)" : v.getName()).append(","));
        buf.append("'}");

        buf.append("text{text=''}");
        buf.append("center{text{type='italic';text=\"Now you're riding with the caravan\"}}");
        buf.append("text{text=''}");

        buf.append(ModQuestions.createAnswerButton2(question, "Confirm"));

        question.getResponder().getCommunicator().sendBml(300, 200, true, true, buf.toString(), 200, 200, 200, question.getTitle());
    }

    @Override
    public void answer(Question question, Properties answers) {
        Village targetVillage;

        try {
            targetVillage = targets.get(Integer.parseInt(answers.getProperty("tgt")));
        } catch (Exception e) {
            performer.getCommunicator().sendAlertServerMessage("Caravan did not leave as expected. Try again later or contact staff.");
            return;
        }
        if (targetVillage == null) {
            performer.getCommunicator().sendAlertServerMessage("Invalid selection.");
            return;
        }

        Item targetPortal = PortalTracker.getPortalFor(targetVillage);
        if (targetPortal == null || targetPortal.zoneId <= 0) {
            performer.getCommunicator().sendAlertServerMessage("Target station is no longer reachable.");
            return;
        }

        TeleportHelper.doTeleport(performer, targetPortal);
    }

    public static void send(Creature player, List<Village> targets) {
        ModQuestions.createQuestion(player, "Town caravan route", "Town caravan route", -10, new TeleportQuestion(player, targets)).sendQuestion();
    }
}
