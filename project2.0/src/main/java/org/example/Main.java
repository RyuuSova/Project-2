package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Entity {
    protected String name;
    protected int x, y;

    public Entity(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class Person extends Entity {
    private boolean isInfected;
    private boolean isCured;
    private boolean isDead;

    public Person(String name, int x, int y) {
        super(name, x, y);
        this.isInfected = false;
        this.isCured = false;
        this.isDead = false;
    }

    public boolean isInfected() {
        return isInfected;
    }

    public void setInfected(boolean infected) {
        isInfected = infected;
    }

    public boolean isCured() {
        return isCured;
    }

    public void setCured(boolean cured) {
        isCured = cured;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public void interact(Entity entity) {
        if (entity instanceof Rat rat && rat.spreadPlague()) {
            this.setInfected(true);
        } else if (entity instanceof PlagueDoctor doctor) {
            doctor.treat(this);
        }
    }

    public void moveRandomly(int fieldSize) {
        Random random = new Random();
        int newX = x + random.nextInt(3) - 1;
        int newY = y + random.nextInt(3) - 1;
        newX = Math.max(0, Math.min(newX, fieldSize - 1));
        newY = Math.max(0, Math.min(newY, fieldSize - 1));
        setPosition(newX, newY);
    }
}

class Rat extends Entity {
    private static final double INFECTION_PROBABILITY = 0.3;

    public Rat(String name, int x, int y) {
        super(name, x, y);
    }

    public boolean spreadPlague() {
        Random random = new Random();
        return random.nextDouble() < INFECTION_PROBABILITY;
    }

    public void moveRandomly(int fieldSize) {
        Random random = new Random();
        int newX = x + random.nextInt(3) - 1;
        int newY = y + random.nextInt(3) - 1;
        newX = Math.max(0, Math.min(newX, fieldSize - 1));
        newY = Math.max(0, Math.min(newY, fieldSize - 1));
        setPosition(newX, newY);
    }
}

class PlagueDoctor extends Person {
    private static final double TREATMENT_SUCCESS_PROBABILITY = 0.95;
    private static final double INFECTION_PROBABILITY = 0.3;
    private static final double RAT_KILL_PROBABILITY = 0.5;

    public PlagueDoctor(String name, int x, int y) {
        super(name, x, y);
    }

    @Override
    public void setInfected(boolean infected) {
        super.setInfected(infected);
    }

    public void treat(Person person) {
        Random random = new Random();
        if (!person.isInfected()) {
            return;
        }
        if (random.nextDouble() < TREATMENT_SUCCESS_PROBABILITY) {
            person.setCured(true);
            person.setInfected(false);
        }
        if (random.nextDouble() < INFECTION_PROBABILITY) {
            this.setInfected(true);
        }
    }

    public void killRat(Rat rat, List<Entity> entities) {
        Random random = new Random();
        if (random.nextDouble() < RAT_KILL_PROBABILITY) {
            entities.remove(rat);
        }
    }
}

public class Main {
    public static final int FIELD_SIZE = 10;

    public static void main(String[] args) {
        List<Entity> entities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int randX = (int) (Math.random() * FIELD_SIZE);
            int randY = (int) (Math.random() * FIELD_SIZE);
            entities.add(new Person("Person" + i, randX, randY));
        }
        for (int i = 0; i < 4; i++) {
            int randX = (int) (Math.random() * FIELD_SIZE);
            int randY = (int) (Math.random() * FIELD_SIZE);
            entities.add(new PlagueDoctor("Doctor" + i, randX, randY));
        }
        for (int i = 0; i < 3; i++) {
            int randX = (int) (Math.random() * FIELD_SIZE);
            int randY = (int) (Math.random() * FIELD_SIZE);
            entities.add(new Rat("Rat" + i, randX, randY));
        }
        simulate(entities);
    }

    private static void simulate(List<Entity> entities) {
        int steps = 100;
        for (int step = 0; step < steps; step++) {
            moveEntities(entities);
            interactEntities(entities);
            updateStatus(entities);
            displayField(entities);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.print("\033[H\033[2J");
            System.out.flush();

            boolean allPeopleDead = entities.stream()
                    .filter(entity -> entity instanceof Person)
                    .map(entity -> (Person) entity)
                    .allMatch(Person::isDead);

            boolean allRatsDead = entities.stream()
                    .noneMatch(entity -> entity instanceof Rat);

            if (allPeopleDead || allRatsDead) {
                displaySummary(entities);
                if (allPeopleDead) {
                    System.out.println("All people are dead. Simulation stops.");
                }
                if (allRatsDead) {
                    System.out.println("All rats are dead. Simulation stops.");
                }
                return; // Возврат из метода simulate
            }
        }
    }

    private static void moveEntities(List<Entity> entities) {
        for (Entity entity : entities) {
            if (entity instanceof Person person && !person.isDead()) {
                person.moveRandomly(FIELD_SIZE);
            } else if (entity instanceof Rat rat) {
                rat.moveRandomly(FIELD_SIZE);
            }
        }
    }

    private static void interactEntities(List<Entity> entities) {
        for (Entity entity : entities) {
            for (Entity other : entities) {
                if (entity != other) {
                    if (entity instanceof Person person && other instanceof Rat rat) {
                        person.interact(rat);
                    } else if (entity instanceof Person person && other instanceof PlagueDoctor doctor) {
                        // Проверяем, находятся ли доктор и крыса на соседних клетках
                        if (Math.abs(person.getX() - doctor.getX()) <= 1 && Math.abs(person.getY() - doctor.getY()) <= 1) {
                            doctor.treat(person);
                        }
                    }
                }
            }
        }
    }

    private static void updateStatus(List<Entity> entities) {
        for (Entity entity : entities) {
            if (entity instanceof Person person && person.isInfected() && !person.isDead()) {
                Random random = new Random();
                if (random.nextDouble() < 0.05) { // 5% probability of dying from infection
                    person.setDead(true);
                }
            }
        }
    }

    private static void displayField(List<Entity> entities) {
        char[][] field = new char[FIELD_SIZE][FIELD_SIZE];
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                field[i][j] = '-';
            }
        }
        for (Entity entity : entities) {
            if (entity instanceof Person person) {
                if (person.isDead()) {
                    field[person.getX()][person.getY()] = 'X'; // Display dead people with 'X'
                } else if (person instanceof PlagueDoctor) {
                    field[person.getX()][person.getY()] = 'D'; // Display doctors with 'D'
                } else if (person.isInfected()) {
                    field[person.getX()][person.getY()] = 'I'; // Display infected people with 'I'
                } else if (person.isCured()) {
                    field[person.getX()][person.getY()] = 'C'; // Display cured people with 'C'
                } else {
                    field[person.getX()][person.getY()] = 'P'; // Display healthy people with 'P'
                }
            } else if (entity instanceof Rat) {
                field[entity.getX()][entity.getY()] = 'R'; // Display rats with 'R'
            }
        }
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                System.out.print(" " + field[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private static void displaySummary(List<Entity> entities) {
        long deadCount = entities.stream()
                .filter(entity -> entity instanceof Person)
                .map(entity -> (Person) entity)
                .filter(Person::isDead)
                .count();

        long curedCount = entities.stream()
                .filter(entity -> entity instanceof Person)
                .map(entity -> (Person) entity)
                .filter(Person::isCured)
                .count();

        long infectedCount = entities.stream()
                .filter(entity -> entity instanceof Person)
                .map(entity -> (Person) entity)
                .filter(Person::isInfected)
                .count();

        System.out.println("Simulation Summary:");
        System.out.println("Total People: " + entities.stream().filter(entity -> entity instanceof Person).count());
        System.out.println("Dead People: " + deadCount);
        System.out.println("Cured People: " + curedCount);
        System.out.println("Infected People: " + infectedCount);
        System.out.println("Total Rats: " + entities.stream().filter(entity -> entity instanceof Rat).count());
    }
}
