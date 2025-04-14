package main

import (
    "fmt"
    "log"
    "os"
    "sync"
    "time"
)

// Task struct
type Task struct {
    ID int
}

func (t Task) Process() {
    time.Sleep(1 * time.Second) // Simulate work
}

// Worker function
func worker(id int, tasks <-chan Task, results chan<- string, wg *sync.WaitGroup) {
    defer wg.Done()
    for task := range tasks {
        log.Printf("Worker-%d started Task ID: %d", id, task.ID)
        t := task
        t.Process()
        results <- fmt.Sprintf("Processed Task ID: %d", t.ID)
        log.Printf("Worker-%d completed Task ID: %d", id, t.ID)
    }
}

func main() {
    const numWorkers = 3
    taskChan := make(chan Task, 10)
    resultChan := make(chan string, 10)
    var wg sync.WaitGroup

    // Start workers
    for i := 1; i <= numWorkers; i++ {
        wg.Add(1)
        go worker(i, taskChan, resultChan, &wg)
    }

    // Add tasks
    for i := 1; i <= 10; i++ {
        taskChan <- Task{ID: i}
    }
    close(taskChan)

    // Wait for all workers
    go func() {
        wg.Wait()
        close(resultChan)
    }()

    // Write results
    file, err := os.Create("results_go.txt")
    if err != nil {
        log.Fatalf("Could not create results file: %v", err)
    }
    defer file.Close()

    for res := range resultChan {
        _, err := file.WriteString(res + "\n")
        if err != nil {
            log.Printf("Error writing result: %v", err)
        }
    }

    log.Println("All tasks processed.")
}

