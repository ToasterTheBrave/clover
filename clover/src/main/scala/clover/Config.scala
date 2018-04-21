package clover

import clover.datastores.InfluxDBStore

object Config {

  def reportLocation(): String = "/home/truppert/projects/master-project/clover-web/public/reports/data"

  def metricSources(): List[MetricSource] = {
    List(
      MetricSource(
        new InfluxDBStore("18.233.132.222", 8086).connect().setDB("rails_app"),
        List(
          Measurement("rails.controller", List("server", "method"), "value"),
          Measurement("rails.db", List("server", "method"), "value"),
          Measurement("rails.view", List("server", "method"), "value")
        )
      ),
      MetricSource(
        new InfluxDBStore("18.233.132.222", 8086).connect().setDB("telegraf"),
        List(
          Measurement("cpu", List("host", "cpu"), "usage_guest"),
          Measurement("cpu", List("host", "cpu"), "usage_guest_nice"),
          Measurement("cpu", List("host", "cpu"), "usage_idle"),
          Measurement("cpu", List("host", "cpu"), "usage_iowait"),
          Measurement("cpu", List("host", "cpu"), "usage_irq"),
          Measurement("cpu", List("host", "cpu"), "usage_nice"),
          Measurement("cpu", List("host", "cpu"), "usage_softirq"),
          Measurement("cpu", List("host", "cpu"), "usage_steal"),
          Measurement("cpu", List("host", "cpu"), "usage_system"),
          Measurement("cpu", List("host", "cpu"), "usage_user"),

          Measurement("disk", List("host", "device"), "free"),
          Measurement("disk", List("host", "device"), "inodes_free"),
          Measurement("disk", List("host", "device"), "inodes_total"),
          Measurement("disk", List("host", "device"), "inodes_used"),
          Measurement("disk", List("host", "device"), "total"),
          Measurement("disk", List("host", "device"), "used"),
          Measurement("disk", List("host", "device"), "used_percent"),

          Measurement("docker", List("host"), "memory_total"),
          Measurement("docker", List("host"), "n_containers"),
          Measurement("docker", List("host"), "n_containers_paused"),
          Measurement("docker", List("host"), "n_containers_running"),
          Measurement("docker", List("host"), "n_containers_stopped"),
          Measurement("docker", List("host"), "n_cpus"),
          Measurement("docker", List("host"), "n_goroutines"),
          Measurement("docker", List("host"), "n_images"),
          Measurement("docker", List("host"), "n_listener_events"),
          Measurement("docker", List("host"), "n_used_file_descriptors"),
          Measurement("docker", List("host"), "pool_blocksize"),

          Measurement("docker_container_blkio", List("container_name"), "io_service_bytes_recursive_async"),
          Measurement("docker_container_blkio", List("container_name"), "io_service_bytes_recursive_read"),
          Measurement("docker_container_blkio", List("container_name"), "io_service_bytes_recursive_sync"),
          Measurement("docker_container_blkio", List("container_name"), "io_service_bytes_recursive_total"),
          Measurement("docker_container_blkio", List("container_name"), "io_service_bytes_recursive_write"),
          Measurement("docker_container_blkio", List("container_name"), "io_serviced_recursive_async"),
          Measurement("docker_container_blkio", List("container_name"), "io_serviced_recursive_read"),
          Measurement("docker_container_blkio", List("container_name"), "io_serviced_recursive_sync"),
          Measurement("docker_container_blkio", List("container_name"), "io_serviced_recursive_total"),
          Measurement("docker_container_blkio", List("container_name"), "io_serviced_recursive_write"),

          Measurement("docker_container_cpu", List("container_name"), "throttling_periods"),
          Measurement("docker_container_cpu", List("container_name"), "throttling_throttled_periods"),
          Measurement("docker_container_cpu", List("container_name"), "throttling_throttled_time"),
          Measurement("docker_container_cpu", List("container_name"), "usage_in_kernelmode"),
          Measurement("docker_container_cpu", List("container_name"), "usage_in_usermode"),
          Measurement("docker_container_cpu", List("container_name"), "usage_percent"),
          Measurement("docker_container_cpu", List("container_name"), "usage_system"),
          Measurement("docker_container_cpu", List("container_name"), "usage_total"),

          Measurement("docker_container_mem", List("container_name"), "active_anon"),
          Measurement("docker_container_mem", List("container_name"), "active_file"),
          Measurement("docker_container_mem", List("container_name"), "cache"),
          Measurement("docker_container_mem", List("container_name"), "hierarchical_memory_limit"),
          Measurement("docker_container_mem", List("container_name"), "inactive_anon"),
          Measurement("docker_container_mem", List("container_name"), "inactive_file"),
          Measurement("docker_container_mem", List("container_name"), "mapped_file"),
          Measurement("docker_container_mem", List("container_name"), "max_usage"),
          Measurement("docker_container_mem", List("container_name"), "pgfault"),
          Measurement("docker_container_mem", List("container_name"), "pgmajfault"),
          Measurement("docker_container_mem", List("container_name"), "pgpgin"),
          Measurement("docker_container_mem", List("container_name"), "pgpgout"),
          Measurement("docker_container_mem", List("container_name"), "rss"),
          Measurement("docker_container_mem", List("container_name"), "rss_huge"),
          Measurement("docker_container_mem", List("container_name"), "total_active_anon"),
          Measurement("docker_container_mem", List("container_name"), "total_active_file"),
          Measurement("docker_container_mem", List("container_name"), "total_cache"),
          Measurement("docker_container_mem", List("container_name"), "total_inactive_anon"),
          Measurement("docker_container_mem", List("container_name"), "total_inactive_file"),
          Measurement("docker_container_mem", List("container_name"), "total_mapped_file"),
          Measurement("docker_container_mem", List("container_name"), "total_pgfault"),
          Measurement("docker_container_mem", List("container_name"), "total_pgmajfault"),
          Measurement("docker_container_mem", List("container_name"), "total_pgpgin"),
          Measurement("docker_container_mem", List("container_name"), "total_pgpgout"),
          Measurement("docker_container_mem", List("container_name"), "total_rss"),
          Measurement("docker_container_mem", List("container_name"), "total_rss_huge"),
          Measurement("docker_container_mem", List("container_name"), "total_unevictable"),
          Measurement("docker_container_mem", List("container_name"), "total_writeback"),
          Measurement("docker_container_mem", List("container_name"), "unevictable"),
          Measurement("docker_container_mem", List("container_name"), "usage"),
          Measurement("docker_container_mem", List("container_name"), "usage_percent"),
          Measurement("docker_container_mem", List("container_name"), "writeback"),

          Measurement("docker_container_net", List("container_name"), "rx_bytes"),
          Measurement("docker_container_net", List("container_name"), "rx_dropped"),
          Measurement("docker_container_net", List("container_name"), "rx_errors"),
          Measurement("docker_container_net", List("container_name"), "rx_packets"),
          Measurement("docker_container_net", List("container_name"), "tx_bytes"),
          Measurement("docker_container_net", List("container_name"), "tx_dropped"),
          Measurement("docker_container_net", List("container_name"), "tx_errors"),
          Measurement("docker_container_net", List("container_name"), "tx_packets"),

          Measurement("docker_data", List("host"), "available"),
          Measurement("docker_data", List("host"), "total"),
          Measurement("docker_data", List("host"), "used"),

          Measurement("docker_metadata", List("host"), "available"),
          Measurement("docker_metadata", List("host"), "total"),
          Measurement("docker_metadata", List("host"), "used"),

          Measurement("kernel", List("host"), "boot_time"),
          Measurement("kernel", List("host"), "context_switches"),
          Measurement("kernel", List("host"), "interrupts"),
          Measurement("kernel", List("host"), "processes_forked"),

          Measurement("mem", List("host"), "active"),
          Measurement("mem", List("host"), "available"),
          Measurement("mem", List("host"), "available_percent"),
          Measurement("mem", List("host"), "buffered"),
          Measurement("mem", List("host"), "cached"),
          Measurement("mem", List("host"), "free"),
          Measurement("mem", List("host"), "inactive"),
          Measurement("mem", List("host"), "slab"),
          Measurement("mem", List("host"), "total"),
          Measurement("mem", List("host"), "used"),
          Measurement("mem", List("host"), "used_percent"),

          Measurement("nginx", List("host", "port"), "accepts"),
          Measurement("nginx", List("host", "port"), "active"),
          Measurement("nginx", List("host", "port"), "handled"),
          Measurement("nginx", List("host", "port"), "reading"),
          Measurement("nginx", List("host", "port"), "requests"),
          Measurement("nginx", List("host", "port"), "waiting"),
          Measurement("nginx", List("host", "port"), "writing"),

          Measurement("processes", List("host"), "blocked"),
          Measurement("processes", List("host"), "dead"),
          Measurement("processes", List("host"), "idle"),
          Measurement("processes", List("host"), "paging"),
          Measurement("processes", List("host"), "running"),
          Measurement("processes", List("host"), "sleeping"),
          Measurement("processes", List("host"), "stopped"),
          Measurement("processes", List("host"), "total"),
          Measurement("processes", List("host"), "total_threads"),
          Measurement("processes", List("host"), "unknown"),
          Measurement("processes", List("host"), "zombies"),

          Measurement("swap", List("host"), "free"),
          Measurement("swap", List("host"), "out"),
          Measurement("swap", List("host"), "total"),
          Measurement("swap", List("host"), "used"),
          Measurement("swap", List("host"), "used_percent"),

          Measurement("system", List("host"), "load1"),
          Measurement("system", List("host"), "load15"),
          Measurement("system", List("host"), "load5"),
          Measurement("system", List("host"), "n_cpus"),
          Measurement("system", List("host"), "n_users"),
          Measurement("system", List("host"), "uptime")
        )
      )
    )
  }

  def cloverStore(): InfluxDBStore = {
    new InfluxDBStore("localhost", 8086).connect()
  }

}
