package repository;

import entity.Player;

import java.util.List;

public class PlayerRepository extends AbstractRepository<Player> {
    public PlayerRepository() {
        super(Player.class);
    }

    @Override
    public void create(Player player) {
        super.create(player);
    }

    @Override
    public Player findById(Integer id) {
        return super.findById(id);
    }

    @Override
    public List<Player> findByName(String name) {
        return super.findByName(name);
    }

}
