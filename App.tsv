import * as Location from "expo-location";
import { Accelerometer, Gyroscope } from "expo-sensors";
import React, { useEffect, useState } from "react";
import {
  ActivityIndicator,
  Alert,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import MapView, { Marker } from "react-native-maps";

export default function App() {
  const [location, setLocation] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [fallPending, setFallPending] = useState(false);
  const [fallDetected, setFallDetected] = useState(false);

  useEffect(() => {
    (async () => {
      let { status } = await Location.requestForegroundPermissionsAsync();
      if (status !== "granted") {
        setErrorMsg("Permission denied");
        setLoading(false);
        return;
      }
      let loc = await Location.getCurrentPositionAsync({});
      setLocation(loc.coords);
      setLoading(false);
    })();
  }, []);

  useEffect(() => {
    const ACCEL_THRESHOLD = 2.5;
    const GYRO_THRESHOLD = 3.0;

    const accelSub = Accelerometer.addListener(({ x, y, z }) => {
      const mag = Math.sqrt(x * x + y * y + z * z);
      if (mag > ACCEL_THRESHOLD && !fallPending && !fallDetected) {
        setFallPending(true);
      }
    });

    const gyroSub = Gyroscope.addListener(({ x, y, z }) => {
      const mag = Math.sqrt(x * x + y * y + z * z);
      if (mag > GYRO_THRESHOLD && !fallPending && !fallDetected) {
        setFallPending(true);
      }
    });

    Accelerometer.setUpdateInterval(500);
    Gyroscope.setUpdateInterval(500);

    return () => {
      accelSub.remove();
      gyroSub.remove();
    };
  }, [fallPending, fallDetected]);

  useEffect(() => {
    if (fallPending) {
      const timer = setTimeout(() => {
        setFallDetected(true);
        setFallPending(false);
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [fallPending]);

  useEffect(() => {
    if (fallDetected) {
      Alert.alert("Fall Detected", "Emergency triggered");
    }
  }, [fallDetected]);

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" />
        <Text>Getting location...</Text>
      </View>
    );
  }

  if (errorMsg) {
    return (
      <View style={styles.centered}>
        <Text style={{ color: "red" }}>{errorMsg}</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <MapView
        style={styles.map}
        initialRegion={{
          latitude: location.latitude,
          longitude: location.longitude,
          latitudeDelta: 0.01,
          longitudeDelta: 0.01,
        }}
      >
        <Marker
          coordinate={{
            latitude: location.latitude,
            longitude: location.longitude,
          }}
          pinColor="blue"
        />

        {fallDetected && (
          <Marker
            coordinate={{
              latitude: location.latitude,
              longitude: location.longitude,
            }}
            pinColor="red"
            title="Emergency Location"
          />
        )}
      </MapView>

      {fallPending && (
        <View style={styles.confirmBox}>
          <Text>Fall detected. Cancel?</Text>
          <TouchableOpacity onPress={() => setFallPending(false)}>
            <Text style={{ color: "blue" }}>CANCEL</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  map: { flex: 1 },
  centered: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
  confirmBox: {
    padding: 12,
    backgroundColor: "#fff3cd",
    alignItems: "center",
  },
});
