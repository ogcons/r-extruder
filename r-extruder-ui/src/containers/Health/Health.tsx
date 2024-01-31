const Health = () => {
  return (
    <pre>
      <code>{JSON.stringify({ status: "UP" }, null, 2)}</code>
    </pre>
  );
};

export default Health;
