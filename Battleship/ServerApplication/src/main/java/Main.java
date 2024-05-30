import entity.Player;
import repository.PlayerRepository;

public class Main {
    public static void main(String[] args) {
        System.out.println("It works!");
        PlayerRepository playerRepository = new PlayerRepository();
        Player player = new Player("John Doe");
        playerRepository.create(player);
    }
}
