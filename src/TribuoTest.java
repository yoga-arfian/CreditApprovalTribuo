
import org.tribuo.classification.dtree.CARTClassificationTrainer;

public class TribuoTest {
    public static void main(String[] args) {
        CARTClassificationTrainer trainer =
                new CARTClassificationTrainer();

        System.out.println("Tribuo berhasil dipanggil!");
        System.out.println("Trainer: " + trainer.getClass().getSimpleName());
    }
}
