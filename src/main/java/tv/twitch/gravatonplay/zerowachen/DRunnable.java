package tv.twitch.gravatonplay.zerowachen;

public abstract class DRunnable implements Runnable {
    String webhook_url;
    public DRunnable(String webhook_url) {
        this.webhook_url = webhook_url;
    }

    @Override
    public void run() {

    }
}
