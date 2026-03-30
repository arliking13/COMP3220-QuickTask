abstract class Task {
    protected String title;
    protected double amount;

    public Task(String title, double amount) {
        this.title = title;
        this.amount = amount;
    }

    public String summary() {
        return getClass().getSimpleName() + ": " + title + " ($" + String.format("%.2f", amount) + ")";
    }
}

class DeliveryTask extends Task {
    public DeliveryTask(String title, double amount) { super(title, amount); }
}

class TutoringTask extends Task {
    public TutoringTask(String title, double amount) { super(title, amount); }
}

class MovingTask extends Task {
    public MovingTask(String title, double amount) { super(title, amount); }
}

class BabysittingTask extends Task {
    public BabysittingTask(String title, double amount) { super(title, amount); }
}

class TaskFactory {
    public static Task createTask(String taskType, String title, double amount) {
        String key = taskType.trim().toLowerCase();
        switch (key) {
            case "delivery":
                return new DeliveryTask(title, amount);
            case "tutoring":
                return new TutoringTask(title, amount);
            case "moving":
                return new MovingTask(title, amount);
            case "babysitting":
                return new BabysittingTask(title, amount);
            default:
                throw new IllegalArgumentException("Unknown task type: " + taskType);
        }
    }
}

interface ContractState {
    void fundEscrow(Contract contract);
    void startWork(Contract contract);
    void completeWork(Contract contract);
    void openDispute(Contract contract);
    String label();
}

class PendingFundingState implements ContractState {
    public void fundEscrow(Contract contract) {
        contract.setEscrowFunded(true);
        contract.setState(new ActiveState());
        System.out.println("Escrow funded. Contract moved to Active state.");
    }

    public void startWork(Contract contract) {
        System.out.println("Cannot start work before escrow is funded.");
    }

    public void completeWork(Contract contract) {
        System.out.println("Cannot complete work before contract becomes active.");
    }

    public void openDispute(Contract contract) {
        System.out.println("Cannot open dispute before the contract becomes active.");
    }

    public String label() { return "PendingFunding"; }
}

class ActiveState implements ContractState {
    public void fundEscrow(Contract contract) {
        System.out.println("Escrow is already funded.");
    }

    public void startWork(Contract contract) {
        System.out.println("Work is already active.");
    }

    public void completeWork(Contract contract) {
        contract.setState(new CompletedState());
        System.out.println("Work completed. Contract moved to Completed state.");
    }

    public void openDispute(Contract contract) {
        contract.setEscrowFrozen(true);
        contract.setState(new DisputedState());
        System.out.println("Dispute opened during active work. Escrow frozen.");
    }

    public String label() { return "Active"; }
}

class CompletedState implements ContractState {
    public void fundEscrow(Contract contract) {
        System.out.println("Escrow action not allowed after completion.");
    }

    public void startWork(Contract contract) {
        System.out.println("Work is already completed.");
    }

    public void completeWork(Contract contract) {
        System.out.println("Contract is already completed.");
    }

    public void openDispute(Contract contract) {
        contract.setEscrowFrozen(true);
        contract.setState(new DisputedState());
        System.out.println("Post-completion dispute opened. Escrow frozen.");
    }

    public String label() { return "Completed"; }
}

class DisputedState implements ContractState {
    public void fundEscrow(Contract contract) {
        System.out.println("Cannot fund escrow while the contract is disputed.");
    }

    public void startWork(Contract contract) {
        System.out.println("Cannot start work while the contract is disputed.");
    }

    public void completeWork(Contract contract) {
        System.out.println("Cannot complete work while the contract is disputed.");
    }

    public void openDispute(Contract contract) {
        System.out.println("Contract is already disputed.");
    }

    public String label() { return "Disputed"; }

    public void resolveDispute(Contract contract, boolean releasePayment) {
        contract.setEscrowFrozen(false);
        contract.setPaymentReleased(releasePayment);
        contract.setState(new ClosedState());
        if (releasePayment) {
            System.out.println("Dispute resolved in favor of worker. Payment released. Contract closed.");
        } else {
            System.out.println("Dispute resolved without payment release. Contract closed.");
        }
    }
}

class ClosedState implements ContractState {
    public void fundEscrow(Contract contract) {
        System.out.println("Closed contract cannot be modified.");
    }

    public void startWork(Contract contract) {
        System.out.println("Closed contract cannot be modified.");
    }

    public void completeWork(Contract contract) {
        System.out.println("Closed contract cannot be modified.");
    }

    public void openDispute(Contract contract) {
        System.out.println("Closed contract cannot be disputed again.");
    }

    public String label() { return "Closed"; }
}

class Contract {
    private Task task;
    private String poster;
    private String worker;
    private boolean escrowFunded;
    private boolean escrowFrozen;
    private boolean paymentReleased;
    private ContractState state;

    public Contract(Task task, String poster, String worker) {
        this.task = task;
        this.poster = poster;
        this.worker = worker;
        this.escrowFunded = false;
        this.escrowFrozen = false;
        this.paymentReleased = false;
        this.state = new PendingFundingState();
    }

    public void fundEscrow() { state.fundEscrow(this); }
    public void startWork() { state.startWork(this); }
    public void completeWork() { state.completeWork(this); }
    public void openDispute() { state.openDispute(this); }

    public void resolveDispute(boolean releasePayment) {
        if (state instanceof DisputedState) {
            ((DisputedState) state).resolveDispute(this, releasePayment);
        } else {
            System.out.println("No dispute exists to resolve.");
        }
    }

    public void status() {
        System.out.println(
            "Task=" + task.title +
            " | State=" + state.label() +
            " | EscrowFunded=" + escrowFunded +
            " | EscrowFrozen=" + escrowFrozen +
            " | PaymentReleased=" + paymentReleased
        );
    }

    public void setEscrowFunded(boolean escrowFunded) { this.escrowFunded = escrowFunded; }
    public void setEscrowFrozen(boolean escrowFrozen) { this.escrowFrozen = escrowFrozen; }
    public void setPaymentReleased(boolean paymentReleased) { this.paymentReleased = paymentReleased; }
    public void setState(ContractState state) { this.state = state; }
}

public class Main {
    private static void factory() {
        System.out.println("Factory Method");
        Task[] tasks = {
            TaskFactory.createTask("delivery", "Deliver package to downtown", 40.0),
            TaskFactory.createTask("tutoring", "Math tutoring for 2 hours", 60.0),
            TaskFactory.createTask("moving", "Help move furniture", 95.0)
        };
        for (Task task : tasks) {
            System.out.println(task.summary());
        }
    }

    private static void stateNormalFlow() {
        System.out.println("\nState: Normal Contract Flow");
        Task task = TaskFactory.createTask("delivery", "Deliver package to downtown", 40.0);
        Contract contract = new Contract(task, "Alice", "Bob");
        contract.status();
        contract.startWork();
        contract.fundEscrow();
        contract.status();
        contract.startWork();
        contract.completeWork();
        contract.status();
    }

    private static void stateDisputeFlow() {
        System.out.println("\nState: Dispute Flow");
        Task task = TaskFactory.createTask("tutoring", "Math tutoring for 2 hours", 60.0);
        Contract contract = new Contract(task, "Carol", "Dave");
        contract.fundEscrow();
        contract.completeWork();
        contract.openDispute();
        contract.status();
        contract.resolveDispute(true);
        contract.status();
    }

    public static void main(String[] args) {
        factory();
        stateNormalFlow();
        stateDisputeFlow();
    }
}
//issues pulling