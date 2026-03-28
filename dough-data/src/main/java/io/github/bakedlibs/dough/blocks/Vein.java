package io.github.bakedlibs.dough.blocks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

// TODO: Refactor this
public final class Vein {

    private static final BlockFace[] faces = new BlockFace[] { BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

    private Vein() {}

    /**
     * This method gives you a List of all Blocks
     * that are directly or indirectly connected to the given Block
     * and share the same Material as the given Block.
     * 
     * @param b
     *            The Block to start with
     * @param limit
     *            The max amount of Blocks to expand into
     * 
     * @return A List of all Blocks
     */
    public static List<Block> find(Block b, int limit) {
        return find(b, limit, block -> block.getType() == b.getType());
    }

    /**
     * This method gives you a List of all Blocks
     * that are directly or indirectly connected to the given Block
     * and pass the given Predicate.
     * 
     * @param b
     *            The Block to start with
     * @param limit
     *            The max amount of Blocks to expand into
     * @param predicate
     *            A Predicate describing what Blocks to count
     * 
     * @return A List of all Blocks
     */
    public static List<Block> find(Block b, int limit, Predicate<Block> predicate) {
        List<Block> list = new ArrayList<>(Math.max(0, limit));
        expand(b, list, limit, predicate);
        return list;
    }

    private static void expand(Block anchor, List<Block> list, int limit, Predicate<Block> predicate) {
        if (limit <= 0) {
            return;
        }

        Set<Block> visited = new HashSet<>(Math.max(16, limit * 2));
        Deque<Block> queue = new ArrayDeque<>();

        visited.add(anchor);
        queue.add(anchor);

        while (!queue.isEmpty() && list.size() < limit) {
            Block current = queue.removeLast();
            list.add(current);

            if (list.size() >= limit) {
                break;
            }

            for (BlockFace face : faces) {
                Block next = current.getRelative(face);

                if (visited.add(next) && predicate.test(next)) {
                    queue.add(next);
                }
            }
        }
    }

}
